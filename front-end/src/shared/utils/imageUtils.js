import imageCompression from "browser-image-compression";
import { createClient } from "@supabase/supabase-js";
import { v4 as uuidv4 } from "uuid";

// Initialize Supabase client
const supabaseUrl = process.env.REACT_APP_SUPABASE_URL || "";
const supabasePublishableKey = process.env.REACT_APP_SUPABASE_PUBLISHABLE_KEY || process.env.REACT_APP_SUPABASE_ANON_KEY || "";

let supabaseClient = null;

export const initSupabase = () => {
  if (!supabaseUrl || !supabasePublishableKey) {
    console.warn("Supabase credentials not configured. Profile pictures will be stored as base64.");
    return null;
  }

  if (!supabaseClient) {
    supabaseClient = createClient(supabaseUrl, supabasePublishableKey);
  }

  return supabaseClient;
};

/**
 * Compress image file to reduce file size
 * @param {File} file - Image file to compress
 * @returns {Promise<File>} Compressed image file
 */
export const compressImage = async (file) => {
  const options = {
    maxSizeMB: 0.5, // Max 500KB after compression
    maxWidthOrHeight: 800, // Max resolution 800px
    useWebWorker: true,
    quality: 0.7, // 70% quality
  };

  try {
    const compressedFile = await imageCompression(file, options);
    return compressedFile;
  } catch (error) {
    console.error("Image compression failed:", error);
    // Return original file if compression fails
    return file;
  }
};

/**
 * Convert file to Base64 data URL
 * @param {File} file - Image file
 * @returns {Promise<string>} Base64 data URL
 */
export const fileToDataUrl = (file) =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result || ""));
    reader.onerror = () => reject(new Error("Unable to read image file."));
    reader.readAsDataURL(file);
  });

/**
 * Upload image to Supabase storage and return public URL
 * @param {File} file - Compressed image file
 * @param {string} username - Username for storage path
 * @returns {Promise<string|null>} Public image URL or null if storage not available
 */
export const uploadImageToSupabase = async (file, username) => {
  const supabase = initSupabase();

  if (!supabase) {
    console.warn("Supabase not configured, falling back to base64 storage.");
    return null;
  }

  try {
    const bucket = "profile-pictures";
    const fileName = `${username}/${uuidv4()}_${Date.now()}.jpg`;

    const { data, error } = await supabase.storage.from(bucket).upload(fileName, file, {
      cacheControl: "3600",
      upsert: false,
    });

    if (error) {
      console.warn("Supabase upload failed, falling back to base64:", error.message);
      return null;
    }

    // Get public URL
    const { data: publicData } = supabase.storage.from(bucket).getPublicUrl(data.path);

    return publicData.publicUrl;
  } catch (error) {
    console.warn("Supabase upload error, falling back to base64:", error.message);
    return null;
  }
};

/**
 * Handle image upload: compress, then try Supabase, fallback to Base64
 * @param {File} file - Image file
 * @param {string} username - Username for storage path
 * @returns {Promise<{type: 'url'|'base64', data: string}>}
 */
export const handleImageUploadWithCompression = async (file, username) => {
  try {
    // Step 1: Compress the image
    const compressedFile = await compressImage(file);

    // Step 2: Try uploading to Supabase
    const publicUrl = await uploadImageToSupabase(compressedFile, username);

    if (publicUrl) {
      return { type: "url", data: publicUrl };
    }

    // Step 3: Fallback to Base64 if Supabase fails
    const dataUrl = await fileToDataUrl(compressedFile);
    return { type: "base64", data: dataUrl };
  } catch (error) {
    console.error("Image upload error:", error);
    throw error;
  }
};

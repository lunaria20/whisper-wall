-- Enable Row Level Security for restriction requests in Supabase.
-- This table is managed through the backend, so no additional client policies are required.

ALTER TABLE IF EXISTS public.restriction_requests ENABLE ROW LEVEL SECURITY;

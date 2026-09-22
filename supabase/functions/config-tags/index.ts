import { corsResponse, jsonResponse } from "../_shared/auth.ts";

Deno.serve((req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "GET") return jsonResponse({ message: "Method not allowed." }, 405);
  return jsonResponse({ tags: ["Relationships", "Career", "Travel", "Music", "Food", "Politics", "Personal Growth", "Movies", "Fitness", "Study", "Friendship", "Life Advice"] });
});

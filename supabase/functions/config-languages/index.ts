import { corsResponse, jsonResponse } from "../_shared/auth.ts";

Deno.serve((req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "GET") return jsonResponse({ message: "Method not allowed." }, 405);
  return jsonResponse({ languages: ["Hindi", "English", "Telugu", "Tamil", "Kannada", "Malayalam", "Marathi", "Bengali", "Gujarati", "Punjabi", "Urdu"] });
});

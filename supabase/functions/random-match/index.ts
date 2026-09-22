import { authenticateRequest, corsResponse, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";
import { eligibleHosts, stringList } from "../_shared/discovery.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const body = await req.json();
    const hosts = await eligibleHosts(userId, stringList(body?.topicTags), stringList(body?.languages), true);
    if (!hosts.length) return jsonResponse({ matched: false, host: null, message: "Abhi matching host available nahi hai. Thodi der baad retry karo." });
    const index = crypto.getRandomValues(new Uint32Array(1))[0] % hosts.length;
    return jsonResponse({ matched: true, host: hosts[index], message: "Host matched successfully." });
  } catch (error) {
    console.error("random-match error", error);
    return requestErrorResponse(error, "Unable to find a match right now.");
  }
});

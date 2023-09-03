/**
   This file copied, and tweaked, from
   https://developers.cloudflare.com/workers/examples/fetch-json/, in the hopes that I can leverage
   it to handle giantbomb requests without having to build a backend. And also in hopes that I can
   deploy this to Cloudflare Pages and it'll "just work"(TM).
 */

export async function onRequest (context) {
  let { request, params, functionPath, env } = context;
  const someHost = "www.giantbomb.com";

  // Rewrite the URL from our hostname to Giant Bomb's hostname
  let url = new URL(request.url);
  url.hostname = someHost;
  url.protocol = 'https';
  url.port = ''

  /**
   * gatherResponse awaits and returns a response body as a string.
   * Use await gatherResponse(..) in an async function to get the response body
   * @param {Response} response
   */
  async function gatherResponse(response) {
    const { headers } = response;
    const contentType = headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
      return JSON.stringify(await response.json());
    }
    return response.text();
  }

  const init = {
    headers: {
      "content-type": "application/json;charset=UTF-8",
      // GiantBomb requires that you set a unique User Agent, or it rejects your request.
      "User-Agent": "It's All Gravie/1.0"
    },
  };

  console.log(`Fetching results from: ${url}`)
  const response = await fetch(url, init);
  const results = await gatherResponse(response);
  return new Response(results, init);
};

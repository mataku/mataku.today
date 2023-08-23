class OGPData {
    description: string;
    imageUrl: string;
    siteName: string;
    title: string;
    url: string;

    constructor() {
        this.description = "";
        this.imageUrl = "";
        this.siteName = "";
        this.title = "";
        this.url = "";
    }
    element(element: Element) {
        switch (element.getAttribute("property")) {
            case "og:description":
                this.description = element.getAttribute("content") ?? "";
                break;
            case "og:image":
                this.imageUrl = element.getAttribute("content") ?? "";
                break;
            case "og:site_name":
                this.siteName = element.getAttribute("content") ?? "";
                break;
            case "og:title":
                this.title = element.getAttribute("content") ?? "";
                break;
            case "og:url":
                this.url = element.getAttribute("content") ?? "";
                break;
            default:
                break;
        }
    }
}


export const onRequest: PagesFunction = async (context) => {
    const url = new URL(context.request.url);
    const params = new URLSearchParams(url.search);
    const href = decodeURIComponent(params.get("url"))
    try {
        const resource = await fetch(href);

        const body = await resource.text();
        const newRes = new Response(body, {
            headers: {
                "Content-Type": "text/html"
            }
        });

        if (resource.ok) {
            const ogp = new OGPData();
            const result = new HTMLRewriter()
                .on("meta", ogp)
                .transform(newRes);

            await result.text()
            const response = JSON.stringify(ogp)

            return new Response(response, {
                headers: {
                    "content-type": "application/json;charset=UTF-8",
                },
            });
        } else {
            return new Response("{}", {
                headers: {
                    "content-type": "application/json;charset=UTF-8",
                },
            });
        }
    } catch (err: any) {
        return new Response("{}", {
            headers: {
                "content-type": "application/json;charset=UTF-8",
            },
        });
    }
}

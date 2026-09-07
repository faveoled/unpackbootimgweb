package app;

import elemental2.core.JsArray;
import elemental2.core.TypedArray;
import elemental2.dom.Blob;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.MouseEvent;
import elemental2.dom.MouseEventInit;
import elemental2.dom.URL;

public class BrowserUtil {

    public static void triggerDownload(String componentName, TypedArray data) {
        JsArray<Blob.ConstructorBlobPartsArrayUnionType> blobParts = new JsArray<>();
        blobParts.push(Blob.ConstructorBlobPartsArrayUnionType.of(data));

        // Pass the content directly into the Blob constructor array without a property bag
        Blob blob = new Blob(blobParts);

        // Generate the temporary object URL
        String blobUrl = URL.createObjectURL(blob);

        // Dynamically build and click the anchor link
        HTMLAnchorElement downloadLink = (HTMLAnchorElement) DomGlobal.document.createElement("a");
        downloadLink.href = blobUrl;
        downloadLink.download = componentName;
        downloadLink.style.display = "none";

        // FIX: Create and dispatch a native click event manually
        MouseEventInit eventInit = MouseEventInit.create();
        eventInit.setBubbles(true);
        eventInit.setCancelable(true);
        MouseEvent clickEvent = new MouseEvent("click", eventInit);

        downloadLink.dispatchEvent(clickEvent);

        DomGlobal.document.body.removeChild(downloadLink);

        // Free memory
        URL.revokeObjectURL(blobUrl);
    }
}

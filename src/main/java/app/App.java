package app;

import elemental2.core.ArrayBuffer;
import elemental2.core.TypedArray;
import elemental2.core.Uint8Array;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.FileReader;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLPreElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableElement;
import elemental2.dom.HTMLTableRowElement;
import elemental2.dom.HTMLTableSectionElement;
import jsinterop.annotations.JsType;

@JsType
public class App {

    public static final String HELLO_WORLD = "Hello J2CL world!";

    private HTMLDivElement wrapper;
    private HTMLPreElement infoArea;
    private HTMLTableElement componentsTable;
    private HTMLTableSectionElement tableBody;
    private HTMLDivElement statusArea;

    public void onModuleLoad() {
        wrapper = (HTMLDivElement) DomGlobal.document.createElement("div");
        wrapper.classList.add("wrapper");

        Element title = DomGlobal.document.createElement("h1");
        title.textContent = "unpack_bootimg for WEB";
        wrapper.appendChild(title);

        HTMLInputElement fileInput = (HTMLInputElement) DomGlobal.document.createElement("input");
        fileInput.type = "file";
        fileInput.accept = ".img";
        fileInput.classList.add("fileInput");
        fileInput.addEventListener("change", this::onFilePicked);
        wrapper.appendChild(fileInput);

        statusArea = (HTMLDivElement) DomGlobal.document.createElement("div");
        statusArea.classList.add("status");
        statusArea.textContent = "Pick a boot.img file to parse.";
        wrapper.appendChild(statusArea);

        Element infoLabel = DomGlobal.document.createElement("h2");
        infoLabel.textContent = "mkbootimg parameters";
        wrapper.appendChild(infoLabel);

        infoArea = (HTMLPreElement) DomGlobal.document.createElement("pre");
        infoArea.classList.add("info");
        wrapper.appendChild(infoArea);

        Element componentsLabel = DomGlobal.document.createElement("h2");
        componentsLabel.textContent = "Components";
        wrapper.appendChild(componentsLabel);

        componentsTable = (HTMLTableElement) DomGlobal.document.createElement("table");
        componentsTable.classList.add("components");
        HTMLTableSectionElement thead = (HTMLTableSectionElement) DomGlobal.document.createElement("thead");
        HTMLTableRowElement headerRow = (HTMLTableRowElement) DomGlobal.document.createElement("tr");
        headerRow.appendChild(makeCell("Name", true));
        headerRow.appendChild(makeCell("Size", true));
        headerRow.appendChild(makeCell("Action", true));
        thead.appendChild(headerRow);
        componentsTable.appendChild(thead);

        tableBody = (HTMLTableSectionElement) DomGlobal.document.createElement("tbody");
        componentsTable.appendChild(tableBody);

        wrapper.appendChild(componentsTable);

        DomGlobal.document.body.appendChild(wrapper);
    }

    private void onFilePicked(Event evt) {
        HTMLInputElement input = (HTMLInputElement) evt.currentTarget;
        clearResults();

        FileList files = input.files;
        if (files == null || files.length == 0) {
            statusArea.textContent = "No file selected.";
            return;
        }

        File file = files.item(0);
        statusArea.textContent = "Parsing " + file.name + " ...";

        FileReader reader = new FileReader();
        reader.addEventListener("load", loadEvt -> onFileLoaded(file, reader));
        reader.addEventListener("error", errorEvt -> statusArea.textContent = "Failed to read file.");
        reader.readAsArrayBuffer(file);
    }

    private void onFileLoaded(File file, FileReader reader) {
        try {
            ArrayBuffer buffer = (ArrayBuffer) reader.result;
            Uint8Array bootImg = new Uint8Array(buffer);
            UnpackResult result = UnpackBootimg.unpackBoot(bootImg);
            renderResult(file.name, result);
        } catch (Throwable t) {
            statusArea.textContent = "Failed to parse boot.img: " + t.getMessage();
        }
    }

    private void renderResult(String fileName, UnpackResult result) {
        Info info = result.getInfo();

        statusArea.textContent = "Parsed " + fileName
                + " (header version " + info.headerVersion + ").";

        infoArea.textContent = info.formatMkBootImg();

        tableBody.innerHTML = "";

        for (BootComponent component : result.getComponents()) {
            HTMLTableRowElement row = (HTMLTableRowElement) DomGlobal.document.createElement("tr");

            row.appendChild(makeCell(component.getName(), false));
            row.appendChild(makeCell(formatSize(component.getSize()), false));

            HTMLTableCellElement actionCell = makeCell("", false);
            HTMLButtonElement downloadBtn = (HTMLButtonElement) DomGlobal.document.createElement("button");
            downloadBtn.classList.add("downloadButton");
            downloadBtn.textContent = "Download";
            TypedArray data = component.getData();
            String componentName = component.getName();
            downloadBtn.addEventListener("click", clickEvt ->
                    BrowserUtil.triggerDownload(
                            fileName + "-" + componentName,
                            data
                    )
            );
            actionCell.appendChild(downloadBtn);
            row.appendChild(actionCell);

            tableBody.appendChild(row);
        }
    }

    private HTMLTableCellElement makeCell(String text, boolean isHeader) {
        String tagName = isHeader ? "th" : "td";
        HTMLTableCellElement cell = (HTMLTableCellElement) DomGlobal.document.createElement(tagName);
        cell.textContent = text;
        return cell;
    }

    private void clearResults() {
        infoArea.textContent = "";
        tableBody.innerHTML = "";
    }

    private static String formatSize(int bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        long kb = bytes / 1024;
        long kbTenths = (bytes % 1024) * 10 / 1024;
        if (kb < 1024) {
            return kb + "." + kbTenths + " KB";
        }
        long mb = kb / 1024;
        long mbTenths = (kb % 1024) * 10 / 1024;
        return mb + "." + mbTenths + " MB";
    }

    public String helloWorldString() {
        return HELLO_WORLD;
    }
}

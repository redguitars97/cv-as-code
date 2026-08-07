package io.github.mattiacozzolino.cvascode.render;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;

import java.nio.file.Path;

public final class PdfRenderer {
    public void render(String html, Path pdf) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
            Page page = browser.newPage();
            page.setContent(html, new Page.SetContentOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
            page.pdf(new Page.PdfOptions()
                    .setPath(pdf)
                    .setFormat("A4")
                    .setPrintBackground(true)
                    .setPreferCSSPageSize(true));
        }
    }
}

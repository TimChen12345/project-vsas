package com.vsas.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>VSAS</title>
                  <style>
                    body { font-family: system-ui, sans-serif; margin: 0; min-height: 100vh;
                      background: #0f1419; color: #e7ecf3; display: flex; align-items: center; justify-content: center; }
                    .box { text-align: center; padding: 2rem; max-width: 32rem; }
                    h1 { font-size: 1.5rem; margin-bottom: 0.5rem; }
                    p { color: #8b9cb3; margin-bottom: 1.5rem; line-height: 1.5; }
                    a { display: inline-block; margin: 0.35rem; padding: 0.6rem 1.1rem; border-radius: 8px;
                      text-decoration: none; font-weight: 600; }
                    .primary { background: #3b82f6; color: #fff; }
                    .secondary { border: 1px solid #2d3a4d; color: #e7ecf3; }
                    code { background: #1a2332; padding: 0.15rem 0.35rem; border-radius: 4px; font-size: 0.9em; }
                  </style>
                </head>
                <body>
                  <div class="box">
                    <h1>VSAS</h1>
                    <p>Virtual library for <strong>digital scrolls</strong>: upload, preview, download, and search.
                    Guests may browse and preview without signing in; upload and download require an account.
                    Default admin: <code>admin</code> / <code>admin123</code>.</p>
                    <p><a class="primary" href="/library.html">Open library</a>
                    <a class="secondary" href="/register.html">Sign up</a>
                    <a class="secondary" href="/login.html">Sign in</a></p>
                  </div>
                </body>
                </html>
                """;
    }
}

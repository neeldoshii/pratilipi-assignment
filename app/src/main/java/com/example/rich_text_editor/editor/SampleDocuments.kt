package com.example.rich_text_editor.editor

object SampleDocuments {

    val SEED_POST_TITLE = "Sample post"

    val SAMPLE_CONTENT_JSON: String =
        """
        {
          "schemaVersion": 2,
          "blocks": [
            {
              "type": "text",
              "heading": "h1",
              "text": "Sample post",
              "spans": []
            },
            {
              "type": "text",
              "heading": "h2",
              "text": "Typography demo",
              "spans": []
            },
            {
              "type": "text",
              "heading": "h3",
              "text": "Lists",
              "spans": []
            },
            {
              "type": "list",
              "ordered": false,
              "items": [
                { "text": "Bullet one", "spans": [] },
                {
                  "text": "Bullet two with italic",
                  "spans": [
                    {
                      "start": 18,
                      "end": 24,
                      "bold": false,
                      "italic": true,
                      "underline": false,
                      "strikethrough": false
                    }
                  ]
                }
              ]
            },
            {
              "type": "list",
              "ordered": true,
              "items": [
                { "text": "Numbered item A", "spans": [] },
                { "text": "Numbered item B", "spans": [] }
              ]
            },
            {
              "type": "text",
              "heading": null,
              "text": "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
              "spans": [
                {
                  "start": 0,
                  "end": 11,
                  "bold": true,
                  "italic": false,
                  "underline": false,
                  "strikethrough": false
                },
                {
                  "start": 153,
                  "end": 286,
                  "bold": true,
                  "italic": false,
                  "underline": false,
                  "strikethrough": false
                },
                {
                  "start": 419,
                  "end": 457,
                  "bold": false,
                  "italic": true,
                  "underline": false,
                  "strikethrough": false
                }
              ]
            },
            {
              "type": "image",
              "fileName": "demo-hero.jpg",
              "widthDp": 280
            },
            {
              "type": "text",
              "heading": null,
              "text": "Caption or continuation after the image.",
              "spans": []
            }
          ]
        }
        """.trimIndent()
}

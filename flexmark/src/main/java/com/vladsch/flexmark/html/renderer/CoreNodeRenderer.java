package com.vladsch.flexmark.html.renderer;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.internal.ListOptions;
import com.vladsch.flexmark.internal.util.Escaping;
import com.vladsch.flexmark.internal.util.ReferenceRepository;
import com.vladsch.flexmark.internal.util.TextCollectingVisitor;
import com.vladsch.flexmark.internal.util.options.Attributes;
import com.vladsch.flexmark.internal.util.options.DataHolder;
import com.vladsch.flexmark.internal.util.sequence.BasedSequence;
import com.vladsch.flexmark.node.*;
import com.vladsch.flexmark.parser.Parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The node renderer that renders all the core nodes (comes last in the order of node renderers).
 */
public class CoreNodeRenderer implements NodeRenderer {
    private final ReferenceRepository referenceRepository;
    private final ListOptions listOptions;

    public CoreNodeRenderer(DataHolder options) {
        this.referenceRepository = options.get(Parser.REFERENCES);
        this.listOptions = new ListOptions(options);
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        return new HashSet<>(Arrays.asList(
                new NodeRenderingHandler<>(AutoLink.class, this::render),
                new NodeRenderingHandler<>(BlockQuote.class, this::render),
                new NodeRenderingHandler<>(BulletList.class, this::render),
                new NodeRenderingHandler<>(Code.class, this::render),
                new NodeRenderingHandler<>(Document.class, this::render),
                new NodeRenderingHandler<>(Emphasis.class, this::render),
                new NodeRenderingHandler<>(FencedCodeBlock.class, this::render),
                new NodeRenderingHandler<>(HardLineBreak.class, this::render),
                new NodeRenderingHandler<>(Heading.class, this::render),
                new NodeRenderingHandler<>(HtmlBlock.class, this::render),
                new NodeRenderingHandler<>(HtmlCommentBlock.class, this::render),
                new NodeRenderingHandler<>(HtmlEntity.class, this::render),
                new NodeRenderingHandler<>(HtmlInline.class, this::render),
                new NodeRenderingHandler<>(HtmlInlineComment.class, this::render),
                new NodeRenderingHandler<>(Image.class, this::render),
                new NodeRenderingHandler<>(ImageRef.class, this::render),
                new NodeRenderingHandler<>(IndentedCodeBlock.class, this::render),
                new NodeRenderingHandler<>(Link.class, this::render),
                new NodeRenderingHandler<>(LinkRef.class, this::render),
                new NodeRenderingHandler<>(BulletListItem.class, this::render),
                new NodeRenderingHandler<>(OrderedListItem.class, this::render),
                new NodeRenderingHandler<>(MailLink.class, this::render),
                new NodeRenderingHandler<>(OrderedList.class, this::render),
                new NodeRenderingHandler<>(Paragraph.class, this::render),
                new NodeRenderingHandler<>(Reference.class, this::render),
                new NodeRenderingHandler<>(SoftLineBreak.class, this::render),
                new NodeRenderingHandler<>(StrongEmphasis.class, this::render),
                new NodeRenderingHandler<>(Text.class, this::render),
                new NodeRenderingHandler<>(ThematicBreak.class, this::render)
        ));
    }

    private void render(Document node, NodeRendererContext context, HtmlWriter html) {
        // No rendering itself
        context.renderChildren(node);
    }

    private void render(Heading node, NodeRendererContext context, HtmlWriter html) {
        if (context.getHtmlOptions().renderHeaderId) {
            String id = context.getNodeId(node);
            if (id != null) {
                html.attr("id", id);
            }
        }

        html.withAttr().tagLine("h" + node.getLevel(), () -> {
            context.renderChildren(node);
        });
    }

    private void render(BlockQuote node, NodeRendererContext context, HtmlWriter html) {
        html.withAttr().tagIndent("blockquote", () -> {
            context.renderChildren(node);
        });
    }

    private void render(FencedCodeBlock node, NodeRendererContext context, HtmlWriter html) {
        BasedSequence info = node.getInfo();
        if (info.isNotNull() && !info.isBlank()) {
            int space = info.indexOf(' ');
            BasedSequence language;
            if (space == -1) {
                language = info;
            } else {
                language = info.subSequence(0, space);
            }
            html.attr("class", context.getHtmlOptions().languageClassPrefix + language.unescape());
        }

        html.line();
        html.tag("pre");
        html.withAttr().tag("code");
        html.text(node.getContentChars().normalizeEOL());
        html.tag("/code");
        html.tag("/pre");
        html.line();
    }

    private void render(ThematicBreak node, NodeRendererContext context, HtmlWriter html) {
        html.withAttr().tagVoidLine("hr");
    }

    private void render(IndentedCodeBlock node, NodeRendererContext context, HtmlWriter html) {
        html.line();
        html.tag("pre");
        html.tag("code");
        html.text(node.getContentChars().trimTailBlankLines().normalizeEndWithEOL());
        html.tag("/code");
        html.tag("/pre");
        html.line();
    }

    private void render(BulletList node, NodeRendererContext context, HtmlWriter html) {
        html.withAttr().tagIndent("ul", () -> {
            context.renderChildren(node);
        });
    }

    private void render(OrderedList node, NodeRendererContext context, HtmlWriter html) {
        int start = node.getStartNumber();
        if (listOptions.orderedStart && start != 1) html.attr("start", String.valueOf(start));
        html.withAttr().tagIndent("ol", () -> {
            context.renderChildren(node);
        });
    }

    private void render(BulletListItem node, NodeRendererContext context, HtmlWriter html) {
        render((ListItem) node, context, html);
    }

    private void render(OrderedListItem node, NodeRendererContext context, HtmlWriter html) {
        render((ListItem) node, context, html);
    }

    private void render(ListItem node, NodeRendererContext context, HtmlWriter html) {
        if (node.getFirstChild() == null || listOptions.isTightListItem(node)) {
            html.withAttr().withCondIndent().tagLine("li", () -> {
                context.renderChildren(node);
            });
        } else {
            html.withAttr().tagIndent("li", () -> {
                context.renderChildren(node);
            });
        }
    }

    private void render(Paragraph node, NodeRendererContext context, HtmlWriter html) {
        boolean inTightList = listOptions.isInTightListItem(node);
        if (!inTightList) {
            html.tagLine("p", () -> {
                context.renderChildren(node);
            });
        } else {
            context.renderChildren(node);
        }
    }

    private void render(Emphasis node, NodeRendererContext context, HtmlWriter html) {
        html.tag("em");
        context.renderChildren(node);
        html.tag("/em");
    }

    private void render(StrongEmphasis node, NodeRendererContext context, HtmlWriter html) {
        html.tag("strong");
        context.renderChildren(node);
        html.tag("/strong");
    }

    private void render(Text node, NodeRendererContext context, HtmlWriter html) {
        html.text(Escaping.normalizeEOL(node.getChars().unescape()));
    }

    private void render(Code node, NodeRendererContext context, HtmlWriter html) {
        html.withAttr().tag("code");
        html.text(Escaping.collapseWhitespace(node.getText(), true));
        html.tag("/code");
    }

    private void render(HtmlBlock node, NodeRendererContext context, HtmlWriter html) {
        renderHtmlBlock(node, context, html, context.getHtmlOptions().suppressHtmlBlocks, context.getHtmlOptions().escapeHtmlBlocks);
    }

    private void render(HtmlCommentBlock node, NodeRendererContext context, HtmlWriter html) {
        renderHtmlBlock(node, context, html, context.getHtmlOptions().suppressHtmlCommentBlocks, context.getHtmlOptions().escapeHtmlCommentBlocks);
    }

    public void renderHtmlBlock(HtmlBlockBase node, NodeRendererContext context, HtmlWriter html, boolean suppress, boolean escape) {
        if (suppress) return;

        html.line();
        if (escape) {
            html.text(node.getContentChars().normalizeEOL());
        } else {
            html.raw(node.getContentChars().normalizeEOL());
        }
        html.line();
    }

    private void render(HtmlInline node, NodeRendererContext context, HtmlWriter html) {
        renderInlineHtml(node, context, html, context.getHtmlOptions().suppressInlineHtml, context.getHtmlOptions().escapeInlineHtml);
    }

    private void render(HtmlInlineComment node, NodeRendererContext context, HtmlWriter html) {
        renderInlineHtml(node, context, html, context.getHtmlOptions().suppressInlineHtmlComments, context.getHtmlOptions().escapeInlineHtmlComments);
    }

    public void renderInlineHtml(HtmlInlineBase node, NodeRendererContext context, HtmlWriter html, boolean suppress, boolean escape) {
        if (suppress) return;

        if (escape) {
            html.text(node.getChars().normalizeEOL());
        } else {
            html.raw(node.getChars().normalizeEOL());
        }
    }

    private void render(SoftLineBreak node, NodeRendererContext context, HtmlWriter html) {
        html.raw(context.getHtmlOptions().softBreak);
    }

    private void render(HardLineBreak node, NodeRendererContext context, HtmlWriter html) {
        html.tagVoid("br").line();
    }

    private void render(Reference node, NodeRendererContext context, HtmlWriter html) {

    }

    private void render(HtmlEntity node, NodeRendererContext context, HtmlWriter html) {
        html.text(node.getChars().unescape());
    }

    private void render(AutoLink node, NodeRendererContext context, HtmlWriter html) {
        String text = node.getText().toString();
        if (context.isDoNotRenderLinks()) {
            html.text(text);
        } else {
            LinkRendering rendering = context.getLinkRendering(LinkType.Link, text, text, Attributes.EMPTY, node);
            html.attr("href", rendering.getUrl())
                    .withAttr(rendering.getAttributes())
                    .tag("a", () -> html.text(rendering.getText()));
        }
    }

    private void render(MailLink node, NodeRendererContext context, HtmlWriter html) {
        String text = node.getText().unescape();
        if (context.isDoNotRenderLinks()) {
            html.text(text);
        } else {
            LinkRendering rendering = context.getLinkRendering(LinkType.MailTo, text, text, Attributes.EMPTY, node);
            html.attr("href", "mailto:" + rendering.getUrl())
                    .withAttr(rendering.getAttributes())
                    .tag("a")
                    .text(rendering.getText())
                    .tag("/a");
        }
    }

    private void render(Image node, NodeRendererContext context, HtmlWriter html) {
        if (!context.isDoNotRenderLinks()) {
            TextCollectingVisitor altTextVisitor = new TextCollectingVisitor();
            node.accept(altTextVisitor);
            String altText = altTextVisitor.getText();
            
            Attributes attributes = new Attributes();
            if (node.getTitle().isNotNull()) {
                attributes.replaceValue("title", node.getTitle().unescape());
            }
            
            LinkRendering rendering = context.getLinkRendering(LinkType.Image, node.getUrl().unescape(), altText, attributes, node);
            html.attr("src", rendering.getUrl());
            html.attr("alt", rendering.getText());
            html.withAttr(rendering.getAttributes()).tagVoid("img");
        }
    }

    private void render(Link node, NodeRendererContext context, HtmlWriter html) {
        if (context.isDoNotRenderLinks()) {
            context.renderChildren(node);
        } else {
            Attributes attributes = new Attributes();
            if (node.getTitle().isNotNull()) {
                attributes.replaceValue("title", node.getTitle().unescape());
            }
            
            LinkRendering rendering = context.getLinkRendering(LinkType.Link, node.getUrl().unescape(), "", attributes, node);
            html.attr("href", rendering.getUrl());
            html.withAttr(rendering.getAttributes()).tag("a");
            context.renderChildren(node);
            html.tag("/a");
        }
    }

    private void render(ImageRef node, NodeRendererContext context, HtmlWriter html) {
        if (!node.isDefined()) {
            // empty ref, we treat it as text
            assert !node.isDefined();
            html.text(node.getChars().unescape());
        } else {
            if (!context.isDoNotRenderLinks()) {
                Reference reference = node.getReferenceNode(referenceRepository);
                assert reference != null;
                TextCollectingVisitor altTextVisitor = new TextCollectingVisitor();
                node.accept(altTextVisitor);
                String altText = altTextVisitor.getText();

                Attributes attributes = new Attributes();
                if (reference.getTitle().isNotNull()) {
                    attributes.replaceValue("title", reference.getTitle().unescape());
                }

                LinkRendering rendering = context.getLinkRendering(LinkType.Image, reference.getUrl().unescape(), altText, attributes, node);
                html.attr("src", rendering.getUrl());
                html.attr("alt", rendering.getText());
                html.withAttr(rendering.getAttributes()).tagVoid("img");
            }
        }
    }

    private void render(LinkRef node, NodeRendererContext context, HtmlWriter html) {
        if (!node.isDefined()) {
            // empty ref, we treat it as text
            assert !node.isDefined();
            html.raw("[");
            context.renderChildren(node);
            html.raw("]");

            if (!node.isReferenceTextCombined()) {
                html.raw("[");
                html.raw(node.getReference().unescape());
                html.raw("]");
            }
        } else {
            if (context.isDoNotRenderLinks()) {
                context.renderChildren(node);
            } else {
                Reference reference = node.getReferenceNode(referenceRepository);
                assert reference != null;
                
                Attributes attributes = new Attributes();
                if (reference.getTitle().isNotNull()) {
                    attributes.replaceValue("title", reference.getTitle().unescape());
                }

                LinkRendering rendering = context.getLinkRendering(LinkType.Link, reference.getUrl().unescape(), "", attributes, node);
                html.attr("href", rendering.getUrl());
                html.withAttr(rendering.getAttributes()).tag("a");
                context.renderChildren(node);
                html.tag("/a");
            }
        }
    }
}

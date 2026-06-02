package hr.doda.vedra.data.xml

/**
 * Pure-Kotlin XML parser. Builds an [XmlNode] tree from a string.
 *
 * Supports: elements, attributes (single & double quoted), self-closing tags,
 * text and CDATA, comments, processing instructions, the XML declaration,
 * basic numeric and named entity references (&amp; &lt; &gt; &quot; &apos;
 * plus &#NN; / &#xNN;).
 *
 * Strips namespace prefixes from element / attribute names so callers can
 * just match on the local name.
 *
 * Not validating, not streaming — fine for the small DHMZ XMLs we ship.
 */
internal object XmlParser {

    fun parse(input: String): XmlNode {
        val p = Cursor(input)
        // Skip leading BOM and whitespace.
        if (p.peek() == '\uFEFF') p.advance()
        skipMisc(p)
        val root = parseElement(p) ?: error("XML has no root element")
        return root
    }

    private fun parseElement(p: Cursor): XmlNode? {
        if (!p.match('<')) return null
        // Should be a real element start.
        val name = readName(p).stripNs()
        val attrs = readAttributes(p)
        if (p.match("/>")) {
            return XmlNode(name = name, attributes = attrs)
        }
        require(p.match('>')) { "Expected '>' at ${p.pos}" }

        val children = mutableListOf<XmlNode>()
        val textBuf = StringBuilder()
        while (true) {
            if (p.startsWith("</")) {
                p.advance(2)
                val end = readName(p).stripNs()
                require(end == name) { "Mismatched closing tag: expected </$name> got </$end>" }
                p.skipWhitespace()
                require(p.match('>')) { "Expected '>' closing </$end>" }
                break
            }
            if (p.startsWith("<!--")) {
                val end = p.indexOf("-->")
                require(end >= 0) { "Unterminated comment" }
                p.jumpTo(end + 3)
                continue
            }
            if (p.startsWith("<![CDATA[")) {
                p.advance(9)
                val end = p.indexOf("]]>")
                require(end >= 0) { "Unterminated CDATA" }
                textBuf.append(p.slice(p.pos, end))
                p.jumpTo(end + 3)
                continue
            }
            if (p.startsWith("<?")) {
                val end = p.indexOf("?>")
                require(end >= 0) { "Unterminated processing instruction" }
                p.jumpTo(end + 2)
                continue
            }
            if (p.peek() == '<') {
                val child = parseElement(p) ?: break
                children += child
                continue
            }
            if (p.eof()) error("Unexpected EOF inside <$name>")
            val ch = p.advance()
            if (ch == '&') {
                textBuf.append(readEntity(p))
            } else {
                textBuf.append(ch)
            }
        }
        return XmlNode(
            name = name,
            attributes = attrs,
            children = children,
            text = textBuf.toString().trim(),
        )
    }

    private fun readAttributes(p: Cursor): Map<String, String> {
        val attrs = linkedMapOf<String, String>()
        while (true) {
            p.skipWhitespace()
            val c = p.peek() ?: error("Unexpected EOF in tag")
            if (c == '/' || c == '>') break
            val name = readName(p).stripNs()
            p.skipWhitespace()
            require(p.match('=')) { "Expected '=' after attribute $name" }
            p.skipWhitespace()
            val quote = p.advance()
            require(quote == '"' || quote == '\'') { "Expected quote for $name" }
            val sb = StringBuilder()
            while (true) {
                val ch = p.peek() ?: error("Unterminated attribute $name")
                if (ch == quote) { p.advance(); break }
                p.advance()
                if (ch == '&') sb.append(readEntity(p)) else sb.append(ch)
            }
            attrs[name] = sb.toString()
        }
        return attrs
    }

    private fun readEntity(p: Cursor): String {
        val sb = StringBuilder()
        while (true) {
            val c = p.peek() ?: error("Unterminated entity")
            p.advance()
            if (c == ';') break
            sb.append(c)
        }
        val raw = sb.toString()
        return when {
            raw == "amp" -> "&"
            raw == "lt" -> "<"
            raw == "gt" -> ">"
            raw == "quot" -> "\""
            raw == "apos" -> "'"
            raw.startsWith("#x") || raw.startsWith("#X") ->
                raw.substring(2).toInt(16).toChar().toString()
            raw.startsWith("#") -> raw.substring(1).toInt().toChar().toString()
            else -> "&$raw;" // unknown entity — keep literal
        }
    }

    private fun readName(p: Cursor): String {
        val start = p.pos
        while (!p.eof()) {
            val c = p.peek()!!
            if (c.isWhitespace() || c == '/' || c == '>' || c == '=' || c == '<') break
            p.advance()
        }
        return p.slice(start, p.pos)
    }

    private fun skipMisc(p: Cursor) {
        while (!p.eof()) {
            p.skipWhitespace()
            when {
                p.startsWith("<?") -> {
                    val end = p.indexOf("?>")
                    require(end >= 0) { "Unterminated processing instruction" }
                    p.jumpTo(end + 2)
                }
                p.startsWith("<!--") -> {
                    val end = p.indexOf("-->")
                    require(end >= 0) { "Unterminated comment" }
                    p.jumpTo(end + 3)
                }
                p.startsWith("<!") -> {
                    val end = p.indexOf(">")
                    require(end >= 0) { "Unterminated DOCTYPE" }
                    p.jumpTo(end + 1)
                }
                else -> return
            }
        }
    }

    private fun String.stripNs(): String {
        val i = indexOf(':')
        return if (i >= 0) substring(i + 1) else this
    }

    private class Cursor(private val src: String) {
        var pos: Int = 0
        fun eof(): Boolean = pos >= src.length
        fun peek(): Char? = src.getOrNull(pos)
        fun advance(): Char = src[pos++]
        fun advance(n: Int) { pos += n }
        fun jumpTo(p: Int) { pos = p }
        fun match(c: Char): Boolean =
            if (peek() == c) { pos++; true } else false
        fun match(s: String): Boolean =
            if (startsWith(s)) { pos += s.length; true } else false
        fun startsWith(s: String): Boolean = src.regionMatches(pos, s, 0, s.length)
        fun indexOf(s: String): Int = src.indexOf(s, pos)
        fun slice(from: Int, to: Int): String = src.substring(from, to)
        fun skipWhitespace() {
            while (!eof() && src[pos].isWhitespace()) pos++
        }
    }
}

fun parseXml(input: String): XmlNode = XmlParser.parse(input)

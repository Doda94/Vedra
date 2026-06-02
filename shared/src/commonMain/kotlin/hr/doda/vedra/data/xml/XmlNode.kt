package hr.doda.vedra.data.xml

/**
 * Minimal DOM-like representation of an XML document. Namespace prefixes
 * are stripped from element names (everything before ':') so callers can
 * lookup tags without worrying about CAP/etc. namespace prefixes.
 */
data class XmlNode(
    val name: String,
    val attributes: Map<String, String> = emptyMap(),
    val children: List<XmlNode> = emptyList(),
    val text: String = "",
) {
    fun child(name: String): XmlNode? = children.firstOrNull { it.name == name }
    fun children(name: String): List<XmlNode> = children.filter { it.name == name }
    fun attr(name: String): String? = attributes[name]
    fun textOf(name: String): String? = child(name)?.text
}

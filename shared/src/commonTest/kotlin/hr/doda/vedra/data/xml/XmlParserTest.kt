package hr.doda.vedra.data.xml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class XmlParserTest {

    @Test
    fun parsesSimpleElement() {
        val node = parseXml("<a x='1'><b>hi</b></a>")
        assertEquals("a", node.name)
        assertEquals("1", node.attr("x"))
        assertEquals("hi", node.child("b")?.text)
    }

    @Test
    fun stripsNamespacePrefix() {
        val node = parseXml(
            "<alert xmlns=\"urn:oasis:names:tc:emergency:cap:1.2\"><id>X</id></alert>"
        )
        assertEquals("alert", node.name)
        assertEquals("X", node.textOf("id"))
    }

    @Test
    fun handlesEntitiesAndCdata() {
        val node = parseXml(
            "<r><a>5 &gt; 3 &amp; ok</a><b><![CDATA[<x>raw</x>]]></b></r>"
        )
        assertEquals("5 > 3 & ok", node.textOf("a"))
        assertEquals("<x>raw</x>", node.textOf("b"))
    }

    @Test
    fun strayAmpersandIsKeptLiteral() {
        val node = parseXml("<r><a>Crikvenica &amp; okolica: sunčano & vedro do 25</a></r>")
        assertEquals("Crikvenica & okolica: sunčano & vedro do 25", node.textOf("a"))
    }

    @Test
    fun numericEntities() {
        val node = parseXml("<r>&#268;akovec &#x10C;</r>")
        assertEquals("Čakovec Č", node.text)
    }

    @Test
    fun selfClosingTags() {
        val node = parseXml("<r><x/><y v='2'/></r>")
        assertEquals(2, node.children.size)
        assertEquals("2", node.child("y")?.attr("v"))
        assertNull(node.child("x")?.attr("v"))
    }

    @Test
    fun ignoresProcessingInstructionAndComment() {
        val node = parseXml(
            "<?xml version='1.0'?><!-- hi --><r>ok</r>"
        )
        assertEquals("r", node.name)
        assertTrue(node.text == "ok")
    }
}

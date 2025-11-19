package com.dhruvbuildz.safepassageapp.Utils

import android.content.Context
import android.content.res.XmlResourceParser
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringWriter
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory
import java.io.StringReader
import org.xml.sax.InputSource

/**
 * Utility to generate network security configuration dynamically
 * This ensures network security config always matches IpConfig
 */
object NetworkSecurityGenerator {
    
    /**
     * Generates network security config XML content
     */
    fun generateNetworkSecurityConfig(): String {
        return """<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
${IpConfig.ALLOWED_DOMAINS.joinToString("\n") { "        <domain includeSubdomains=\"true\">$it</domain>" }}
    </domain-config>
    
    <!-- Allow cleartext traffic for local development -->
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system"/>
        </trust-anchors>
    </base-config>
</network-security-config>"""
    }
    
    /**
     * Gets the current network security config as a string
     */
    fun getCurrentConfig(): String {
        return generateNetworkSecurityConfig()
    }
}

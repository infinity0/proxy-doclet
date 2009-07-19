#!/bin/sh
if [ -z "$1" ]; then echo "give the source path"; exit 2; fi;

DOCLET=src/info/bliki/doclet
rm -rf "$DOCLET" && mkdir -p "$DOCLET"
mkdir -p "$DOCLET/internal/toolkit"
cp -R "$1/com/sun/tools/doclets/formats" "$DOCLET" || exit $?
cp -R "$1/com/sun/tools/doclets/standard" "$DOCLET" || exit $?
cp -R "$1/com/sun/tools/doclets/internal/toolkit/AbstractDoclet.java" "$DOCLET/internal/toolkit" || exit $?

patch -p0 << EOF
--- src/info/bliki/doclet/formats/html/HtmlDocletWriter.java	2009-04-24 08:35:09.000000000 +0100
+++ src/info/bliki/doclet/formats/html/HtmlDocletWriter.java	2009-07-18 23:17:07.000000000 +0100
@@ -48,6 +48,56 @@
 public class HtmlDocletWriter extends HtmlDocWriter {
 
     /**
+     * Wiki renderer
+     */
+    public info.bliki.wiki.model.WikiModel wiki = new info.bliki.wiki.model.WikiModel("", "");
+
+    public String preprocessWikiString(String b) {
+        String lines[] = b.split("\\\\n");
+        int indent=Integer.MAX_VALUE;
+        // find smallest indent in the first 4 non-blank lines, excl the first line
+        // whose indent is discarded by javadoc
+        for (int i=1, l=0; i<lines.length && l<4; ++i) {
+            int j=-1;
+            while (++j < lines[i].length() && lines[i].charAt(j) == ' ');
+            if (j == lines[i].length()) { continue; }
+            if (j < indent) { indent = j; }
+            ++l;
+        }
+        boolean startedtext = false;
+        boolean startparagraph = true;
+        char firstchar = 0;
+        StringBuffer result = new StringBuffer();
+        // selectively merge lines into paragraphs
+        for (int i=0; i<lines.length; ++i) {
+            int j=-1;
+            while (++j < lines[i].length() && lines[i].charAt(j) == ' ');
+            if (j == lines[i].length()) {
+                // if we haven't seen text yet, skip the entire line
+                if (!startedtext) { continue; }
+                // if line is empty, start a new paragraph
+                result.append('\n').append('\n');
+                startparagraph = true;
+                continue;
+            } else if (startparagraph) {
+                // determine the automatic line-merge setting from the first character of
+                // each paragraph
+                startparagraph = false;
+                firstchar = (j > indent)? ' ': Character.isLetter(lines[i].charAt(j))? 0: lines[i].charAt(j);
+            } else if (lines[i].charAt(indent > j? j: indent) == firstchar) {
+                // if firstchar is ' ', start a new line on ' '
+                // if firstchar is a letter, start a new line on \0 (effectively never)
+                // if firstchar is a symbol, start a new line on that same symbol
+                result.append('\n');
+            }
+            startedtext = true;
+            // strip the indent (or smaller) from each line
+            result.append(lines[i].substring(firstchar != ' '? j: indent > j? j: indent)).append(' ');
+        }
+        return result.toString();
+    }
+
+    /**
      * Relative path from the file getting generated to the destination
      * directory. For example, if the file getting generated is
      * "java/lang/Object.html", then the relative path string is "../../".
@@ -1494,7 +1544,10 @@
                 result.append(textBuff);
             }
         }
-        return result.toString();
+        String s = wiki.render(preprocessWikiString(result.toString()));
+        // strip <p> tags from single-line comments
+        return (s.lastIndexOf("<p>") == 1 && s.startsWith("\n<p>") && s.endsWith("</p>"))? "\n" + s.substring(4, s.length()-4): s;
+        //return preprocessWikiString(result.toString());
     }
 
     /**
--- src/info/bliki/doclet/internal/toolkit/AbstractDoclet.java	2009-07-18 18:59:17.000000000 +0100
+++ src/info/bliki/doclet/internal/toolkit/AbstractDoclet.java	2009-07-18 18:18:54.000000000 +0100
@@ -23,10 +23,11 @@
  * have any questions.
  */
 
-package com.sun.tools.doclets.internal.toolkit;
+package info.bliki.doclet.internal.toolkit;
 
 import com.sun.tools.doclets.internal.toolkit.builders.*;
 import com.sun.tools.doclets.internal.toolkit.util.*;
+import com.sun.tools.doclets.internal.toolkit.*;
 import com.sun.javadoc.*;
 import java.util.*;
 import java.io.*;

--- src/info/bliki/doclet/formats/html/HtmlDoclet.java	2009-07-18 18:59:17.000000000 +0100
+++ src/info/bliki/doclet/formats/html/HtmlDoclet.java	2009-07-18 18:26:28.000000000 +0100
@@ -40,7 +40,7 @@
  * @author Jamie Ho
  *
  */
-public class HtmlDoclet extends AbstractDoclet {
+public class HtmlDoclet extends info.bliki.doclet.internal.toolkit.AbstractDoclet {
     public HtmlDoclet() {
         configuration = (ConfigurationImpl) configuration();
     }
EOF
find src -type f | xargs perl -pi -e 's/com.sun.tools.doclets.formats/info.bliki.doclet.formats/g'
find src -type f | xargs perl -pi -e 's/com.sun.tools.doclets.standard/info.bliki.doclet.standard/g'
find src -type f | xargs perl -pi -e 's/info.bliki.doclet.formats.html.resources.standard/com.sun.tools.doclets.formats.html.resources.standard/g'
cat > "$DOCLET/src.properties" <<EOF
# Source automatically generated from $1
# by bliki-doclet.mksrc on $(date)
path=$1
base=$(basename "$1")
dir=$(dirname "$1")
EOF

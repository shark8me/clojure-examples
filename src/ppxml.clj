;from https://gist.github.com/sritchie/895482
(ns ppxml
  (:import [java.io StringReader StringWriter]
           [javax.xml.transform TransformerFactory OutputKeys]
           [javax.xml.transform.stream StreamSource StreamResult]))

(defn ppxml
  "Accepts an XML string with no newline formatting and returns the
 same XML with pretty-print formatting, as described by Nurullah Akaya
 in [this post](http://goo.gl/Y9OVO)."
  [xml-str]
  (let [in  (StreamSource. (StringReader. xml-str))
        out (StreamResult. (StringWriter.))
        transformer (.newTransformer
                     (TransformerFactory/newInstance))]
    (doseq [[prop val] {OutputKeys/INDENT "yes"
                        OutputKeys/METHOD "xml"
                        "{http://xml.apache.org/xslt}indent-amount" "2"}]
      (.setOutputProperty transformer prop val))
    (.transform transformer in out)
    (str (.getWriter out))))

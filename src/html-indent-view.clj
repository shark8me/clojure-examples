(ns html-indent-view
  (:gen-class )
  (:import
           [java.io StringWriter])
  (:require [net.cgrand.enlive-html :as html]
            [hiccup.core :as hc]
              [gorilla-renderable.core :refer [render Renderable]]
   [gorilla-repl.html :refer [html-view]]
            [gorilla-repl.table :refer [table-view]]
            [clojure.xml :as x]
            [clojure.zip :as z]
            [zip.visit :as zv]
            [clojure.core.match :refer [match]]
            [clojure.java.io :as io]))

(zv/defvisitor collect-tags :pre [n s]
  (do ;(println (z/path (z/node n)))
  (if-let [tag (:tag n)]
    (let [con (first (:content n))
          mf {:tag tag :door :open}
          mf1 (if-let [att (:attrs n)]
                (assoc mf :attrs att) mf)
          mf2 (if (instance? java.lang.String con )
                (assoc mf1 :content [con]) mf1)]
    {:state (into s [mf2])}))))
(zv/defvisitor ct2 :post [n s]
  (if-let [tag (:tag n)]
    {:state (into s [{:tag tag :door :close}])}))

(defn redfn2
  [{:keys [x prevn] :as m} y ]
  (let [n (last prevn)]
    (assoc {} :x y
      :prevn (conj prevn
               (match [x y]
                      [:open :open] (inc n)
                      [:close :close] (dec n)
                      :else n)))))

(defn get-indent-seq
  [root]
  (let [kv (:state (zv/visit root [] [collect-tags ct2 ]))
        oc (mapv :door kv)]
    (-> (reduce redfn2 {:prevn [0] :x :close } oc)
      :prevn rest)))

(defn emit-element
  "emits a single tag, either starting or ending, given a map "
  [{:keys [tag door content attrs] :as m}]
  (with-open [w (StringWriter.)]
    (if (= door :open)
        (do
          (.write w (str "<" (name tag)))
          (if attrs
            (doseq [attr attrs]
              (.write w (str " " (name (key attr)) "='" (val attr)"'"))))
          (.write w ">")
          (if content
            (.write w (first content)))
          )
       (do
          (.write w (str "</" (name tag) ">"))
          ))
      (.toString w)))

(defn get-html-content
  "returns a seq of html tags that are indented "
  [s]
  (let [root (z/xml-zip (x/parse (java.io.ByteArrayInputStream. (.getBytes s))))
         indent-str "  "
        kv (:state (zv/visit root [] [collect-tags ct2 ]))
        els  (mapv emit-element kv)
        indentseq  (get-indent-seq root)
        fnx (fn[i el] (str (clojure.string/join (take i (repeat indent-str))) el))]
    (mapv fnx indentseq els)))

(defrecord HtmlIndentView [contents opts])
(defn- escape-html
  [str]
  ;; this list of HTML replacements taken from underscore.js
  ;; https://github.com/jashkenas/underscore
  (clojure.string/escape str {\& "&amp;", \< "&lt;", \> "&gt;", \" "&quot;", \' "&#x27;"}))

(defn- span-render
  [thing]
  {:type :html
   :content (str "<span class='unk'>" (escape-html thing) "</span>")
   :value (pr-str thing)})

(defn html-indent-view [contents & opts]
  (HtmlIndentView. contents opts))

(extend-type HtmlIndentView
  Renderable
  (render [self]
          (let [inp ["the" "quick" "brown" "fox"]]
            (println " self "  self)
             {:type :list-like
     :open (str "<span class='clj-nil'></span>")
     :close "<span class='clj-nil'></span>"
     :separator "\n"
     :items (map span-render (get-html-content (:contents self)))
	:value (pr-str self)}
            )))


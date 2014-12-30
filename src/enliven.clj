(ns enliven
  (:require [net.cgrand.enlive-html :as html]
            [hiccup.core :as hc]
            [ppxml :as ppx]
            [clojure.java.io :as io]))


;simple tutorial of Enlive that uses Hiccup to generate html content
;We'll create some html snippets to start with
(hc/html [:div])
(hc/html [:div [:h1]])
;lets add some text to the h1 tag
(hc/html [:div [:h1 "head"]])
;add an attribute to the h1 tag
(hc/html [:div [:h1 {:class "url"} "Heading" ]])

;we'll use the sniptest macro for quick experimentation
;first create an html snippet that will be modified
(def snip1 (hc/html [:div]))
snip1

;lets add a class attribute to the snip1
(html/sniptest snip1 (html/add-class "abc"))

;add text to the div tag
(html/sniptest snip1 (html/content "dev"))

;create a larger snippet with deeper structure

(def snip2 (hc/html [:div {:class "url"}
          [:a {:href "http://clojure.org"} "Clojure - home"]] ))
snip2

;the third argument is a selector that chooses the target node for the transformation
;here we change the href url
(html/sniptest snip2 [:div :a] (html/set-attr :href "http://clojuredocs.org"))

;lets create a snippet that has a list of items
(def snip3 (hc/html [:body [:h1 "heading"]
          [:ul
           [:li "one"]
           [:li "two"]]]))
snip3

;change the content of the li tags
(html/sniptest snip3
               [:body :ul :li]
               (html/content "abcd"))

;there's no need to give the path from root till the leaf node, omit the parent tags (body,ul) tag and it still works.
(html/sniptest snip3
               [:ul :li]
               (html/content "abcd"))
(html/sniptest snip3
               [:li]
               (html/content "abcd"))

;note that it changed the content of all the li tags
;lets try to change (the content of) a specific tag, the first tag.
(html/sniptest snip3
               [[:li html/first-child]] (html/content "abcd"))

;we can instead choose the first child of a given type, and specify the type as
;a listindex
(html/sniptest snip3
               [[html/first-of-type :li]] (html/content "abcd"))

;Lets try to replace the list with one of our own.
;Define the data, a list of movies directed by Vishal Bharadwaj
(def data ["Omkara","Blue Umbrella","Kaminey","Haider"])
data

;do the replacement
(html/sniptest snip3
               ;match the first list index
               [[html/first-of-type :li]]
               ;clone the first list index, and for each element in data, add a new list index
               (html/clone-for [item data]
                               [:li] (html/content item)))

;almost what we want, except that the last element of the list was unnecessary
;We can keep only one listitem in the template and achieve the same result


;the last example, where we have a 2 level structure of data.It consists of a title, followed by a list.
;Each item in the list has a text and href link associated with it
(def data2 [{:title "Designation", :data [{:text "New Hire (29)", :href "www.google.com"}
                               {:text "Manager (11)", :href "www.google.com"} ]}
           {:title "Group", :data [{:text "IT (20)", :href "www.google.com"}
                             {:text "Customer Advocacy (10)", :href "www.google.com"}]}])
data2

;define the html snippet
;it consists of a list of h1 titles, followed by a unnumbered list.
;each listitem is a link
(def snip3 (hc/html [:body [:h1 {:class "title"} "heading"]
          [:ul {:id "grouplevel"} [:li [:a {:href "cnn.com"} "content"]]]]))
snip3


;There are 2 tasks to be accomplished,
;set the title in the h1 tag
;create a list of li (listitems)

;the first data items is:
(first data2)


(html/sniptest snip3
  ;select the tags from the <h1> tag till the <ul class="grouplevel"> tag
  {[:h1][[html/first-of-type :ul#grouplevel]]}
               ;for each list item in data2,
               (html/clone-for [{:keys [title data]} data2]
                               ;set the content for the <h1> tag
                               [:h1] (html/content title)
                               ;for the <li> items
                               [:ul#grouplevel [:li html/first-of-type]]
                               ;for all the items in the :data section, create a <li><a> tag
                               (html/clone-for [{:keys [text href]} data]
                                                [:li :a] (html/set-attr :href href)
                                                [:li :a] (html/content text)
                                                )))

;we can also do this in 2 phases, using the snippet and template functionality
;we first define a snippet that will act on one item in our data.
(html/defsnippet h1snip
  (html/html-snippet snip3)
  ;select the <h1> to <ul id="grouplevel"> tags
  {[:h1][[html/first-of-type :ul#grouplevel]]}
  ;the single argument is destructured and title, data keys are separated
  [{:keys [title data]}]
    ;set the <h1> tag content
    [[html/first-of-type :h1]] (html/content title)
    ;select the <ul id="grouplevel"> tag that has a <li> child
    [:ul#grouplevel [html/first-of-type :li ]]
    ;set the <li> tags for each items in data
      (html/clone-for [{:keys [href text]} data]
                              [:li :a] (html/set-attr :href href)
                              [:li :a] (html/content text))

     )

;we can run the snippet on any items in the data2 list
(h1snip (second data2))


;define a template that will do the top level stuff
(html/deftemplate te1
  ;acts on html with snip3 format, and takes an argument containing the data that we wish to replace
  (html/html-snippet snip3) [mydata]
  ;select the body
  [:body]
  ;run the transform where the h1snip snippet function transforms each data item
  (html/content (map h1snip mydata)))


;concat all the str output to create the result
(reduce str (te1 data2))

(ns ogres.app.dom)

(defn user-type []
  (let [search (.. js/window -location -search)
        params (js/URLSearchParams. search)]
    (cond (= (.get params "share") "true") :view
          (string? (.get params "join"))   :conn
          :else                            :host)))

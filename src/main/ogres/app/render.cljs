(ns ogres.app.render
  (:require [ogres.app.const :refer [PATH]]
            [ogres.app.hooks :refer [use-image]]
            [ogres.app.provider.storage :refer [initialize]]
            [uix.core :refer [defui $ create-error-boundary use-insertion-effect]]))

(defn ^:private create-range
  [min max val]
  (cond (< (- max min) 7)
        (range min (inc max))
        (<= val (+ min 3))
        [min (+ min 1) (+ min 2) (+ min 3) (+ min 4) :spacel max]
        (>= val (- max 3))
        [min :spacel (- max 4) (- max 3) (- max 2) (- max 1) max]
        :else
        [min :spacel (- val 1) val (+ val 1) :spacer max]))

(defui icon [{:keys [name size] :or {size 22}}]
  ($ :svg {:class "icon" :width size :height size :fill "currentColor" :role "presentation"}
    ($ :use {:href (str PATH "/icons.svg" "#icon-" name)})))

(def error-boundary
  (create-error-boundary
   {:derive-error-state (fn [error] {:error error})}
   (fn [[state _] {:keys [children]}]
     (let [store (initialize)]
       (if (:error state)
         ($ :.layout-error
           ($ :.layout-error-content
             ($ :div {:style {:margin-top 4 :color "hsl(6, 73%, 60%)"}}
               ($ icon {:name "exclamation-triangle-fill"}))
             ($ :div
               ($ :div {:style {:font-size 20 :line-height 1}}
                 "The application crashed unexpectedly.")
               ($ :div {:style {:margin-top 16 :margin-bottom 8 :max-width 512}}
                 "Try refreshing the page; if that doesn't work you may have to
                  delete your local data. This will reset the application to
                  its original state, removing your data and images.")
               ($ :button.button.button-danger
                 {:on-click
                  #(.then
                    (.delete store)
                    (.reload (.-location js/window)))} "Delete your local data")
               ($ :nav {:style {:margin-top 32 :color "var(--color-danger-500)"}}
                 ($ :ul {:style {:display "flex" :gap 16}}
                   ($ :li ($ :a {:href "/"} "Home"))
                   ($ :li ($ :a {:href "https://github.com/samcf/ogres/wiki"} "Wiki"))
                   ($ :li ($ :a {:href "https://github.com/samcf/ogres/discussions"} "Support")))))))
         children)))))

(defui image [{:keys [checksum children]}]
  (let [data-url (use-image checksum)]
    (children {:data-url data-url})))

(defui pagination
  [{:keys [name pages value on-change]
    :or   {pages 10 value 1 on-change identity}}]
  ($ :nav {:role "navigation"}
    ($ :ol.pagination
      ($ :li
        ($ :button
          {:aria-disabled (= value 1)
           :on-click
           (fn []
             (if (not (= value 1))
               (on-change (dec value))))}
          ($ icon {:name "chevron-left" :size 16})))
      (for [term (create-range 1 pages value)]
        ($ :li {:key term}
          (if (or (= term :spacel) (= term :spacer))
            ($ :label \…)
            ($ :label
              ($ :input
                {:type "radio"
                 :name name
                 :value value
                 :checked (= term value)
                 :on-change #(on-change term)}) term))))
      ($ :li
        ($ :button
          {:aria-disabled (= value pages)
           :on-click
           (fn []
             (if (not (= value pages))
               (on-change (inc value))))}
          ($ icon {:name "chevron-right" :size 16}))))))

(defui stylesheet [props]
  (let [{:keys [name]} props]
    (use-insertion-effect
     (fn []
       (let [element (js/document.createElement "link")]
         (set! (.-href element) (str PATH "/" name))
         (set! (.-rel element) "stylesheet")
         (.. js/document -head (appendChild element)))) [name])) nil)

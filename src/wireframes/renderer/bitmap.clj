(ns wireframes.renderer.bitmap
  (:use [wireframes.renderer :only [get-3d-points get-2d-points priority-fill
                                    calculate-illumination shader]])
  (:require [wireframes.common :as c]
            [wireframes.transform :as t])
  (:import [java.awt.image BufferedImage]
           [java.awt.geom AffineTransform GeneralPath Ellipse2D$Double]
           [java.awt Color Graphics2D RenderingHints BasicStroke GraphicsEnvironment]
           [javax.imageio ImageIO]))

(def create-color
  (memoize
    (fn [^long brightness]
      (let [brightness (Math/max 5 (Math/min brightness 250))]
        (Color. brightness brightness brightness)))))

(defn- draw-dot [^Graphics2D g2d [^double x ^double y] size]
  (.fill g2d (Ellipse2D$Double. (- x (/ size 2)) (- y (/ size 2)) size size)))

(defn- add-line [^GeneralPath path [^double ax ^double ay] [^double bx ^double by]]
  (doto path
    (.moveTo ax ay)
    (.lineTo bx by))
  path)

(defn- create-polygon [^GeneralPath path [[^double ax ^double ay] & more]]
  (doto path
    (.reset)
    (.moveTo ax ay))
  (doseq [[^double bx ^double by] more]
    (.lineTo path bx by))
  (.closePath path)
  path)

(defn draw-wireframe [{:keys [focal-length transform shape]} ^Graphics2D g2d]
  (let [path (GeneralPath.)
        points-3d (get-3d-points transform shape)
        points-2d (get-2d-points focal-length points-3d)]

    ;(.setColor g2d Color/RED)
    ;(doseq [[idx1 idx2] (:lines shape)]
    ;  (draw-dot g2d (points-2d idx1) 0.008))

    ;(.setColor g2d Color/GREEN)
    ;(doseq [[idx1 idx2 idx3] (:polygons shape)]
    ;  (add-line path (points-2d idx1) (points-2d idx2))
    ;  (add-line path (points-2d idx1) (points-2d idx3))
    ;  (add-line path (points-2d idx2) (points-2d idx3))
    ;  )
    ;(.draw g2d path)
    ;(.reset path)

    (.setColor g2d Color/BLACK)
    (doseq [line (:lines shape)]
      (apply add-line path (map points-2d line)))
    (.draw g2d path)))

(defn reduce-polygons [polygons]
  (loop [acc []
         polygons polygons]
    (if (empty? polygons)
      acc
      (let [[p & ps] (t/triangulate (first polygons))]
        (recur
          (conj acc p)
          (c/simple-concat ps (next polygons)))))))

(defn draw-solid [{:keys [focal-length transform shape fill-color]} ^Graphics2D g2d]
  (let [path (GeneralPath.)
        points-3d (get-3d-points transform shape)
        points-2d (get-2d-points focal-length points-3d)
        shader    (shader points-3d create-color)
        polygons  (cond
                    (and fill-color (zero? (.getAlpha fill-color))) (:polygons shape)
                    (and fill-color) (sort-by (priority-fill points-3d) (:polygons shape))
                    :else (sort-by (priority-fill points-3d) (reduce-polygons (:polygons shape))))]
    (doseq [polygon polygons
            :let [p (create-polygon path (map points-2d polygon))
                  face-color (if fill-color fill-color (shader polygon))
                  edge-color (if fill-color Color/BLACK face-color)]]
        (doto g2d
          (.setColor face-color)
          (.fill p)
          (.setColor edge-color)
          (.draw p)))))

(defn create-image [w h]
  (if (GraphicsEnvironment/isHeadless)
    (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
    (.createCompatibleImage
       (.getDefaultConfiguration
         (.getDefaultScreenDevice
           (GraphicsEnvironment/getLocalGraphicsEnvironment)))
       w h)))

(defn ->img [draw-fn [w h]]
  (let [img (create-image w h)
        g2d (.createGraphics img)
        scale (min (quot w 2) (quot h 2))]
    (doto g2d
      (.setBackground Color/WHITE)
      (.clearRect 0 0 w h)
      (.setColor Color/BLACK)
      (.translate (quot w 2) (quot h 2))
      (.scale scale scale)
      (.setStroke (BasicStroke. (/ 1.0 (quot w 2))))
      (.setRenderingHint RenderingHints/KEY_STROKE_CONTROL RenderingHints/VALUE_STROKE_NORMALIZE)
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
      (.setRenderingHint RenderingHints/KEY_RENDERING RenderingHints/VALUE_RENDER_QUALITY))
    (draw-fn g2d)
    (.dispose g2d)
    img))

(defn write-png [image filename]
  (ImageIO/write image "png" (clojure.java.io/file filename)))

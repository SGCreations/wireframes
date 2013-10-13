(ns wireframes.shapes.wavefront-loader-tests
  (:use [clojure.test]
        [wireframes.shapes.wavefront-loader]))

(deftest vertex-matcher-test
  (is (= {:points [[-0.000000 6.350881 3.173125]]}
         (vertex-matcher "-0.000000 6.350881 3.173125")))

  (is (= {:points [[1.288484 6.238661 1.455338]]}
         (vertex-matcher "1.288484 6.238661 1.455338")))

  (is (= {:points [[nil]]} ; FIXME possibly not right, should be nil..
         (vertex-matcher ""))))

(deftest face-matcher-test
  ; discard any polygons with less than two faces
  (is (nil? (face-matcher "43/1/1 42/4/2")))

  (is (= {:polygons [[42 41 51 50]]}
         (face-matcher "43/1/1 42/4/2  52/3/3 51/2/4")))

  (is (= {:polygons [[42 41 51 50]]}
         (face-matcher "43/1/1 42//2  52//3 51/2/")))

  (is (= {:polygons [[42 41 51 50]]}
         (face-matcher "43 42 52  51"))))

(deftest parse-line-test
  (is (= {:polygons [[42 41 51 50]]}
         (parse-line directives "f 43/1/1 42/4/2 52/3/3 51/2/4")))

  (is (= {:polygons [[42 41 51 50]]}
         (parse-line directives "f   43/1/1  42/4/2   52/3/3   51/2/4")))

  (is (= {:points [[1.288484 6.238661 1.455338]]}
         (parse-line directives "v 1.288484 6.238661 1.455338")))

  (is (= {:points [[-4.102479 21.410454 -2.796852]]}
         (parse-line directives "v  -4.102479 21.410454 -2.796852")))

  (is (= {:points [[-4.102479 21.410454 -2.796852]]}
         (parse-line directives "v  -4.102479   21.410454     -2.796852")))

  ; normals are not supported
  (is (nil? (parse-line directives "vn -0.687989 0.724467 0.042657")))

  ; texture-coords  are not supported
  (is (nil? (parse-line directives "vt 0.126073 0.705015")))

  ; smooth shading is not supported
  (is (nil? (parse-line directives "s off")))

  (is (nil? (parse-line directives "# comment")))

  (is (nil? (parse-line directives ""))))

(deftest load-shape-test
  (let [shape (load-shape "resources/obj_IconA5.obj")]
    (is (= 31734  (count (:points shape))))
    (is (= 31385  (count (:polygons shape))))))

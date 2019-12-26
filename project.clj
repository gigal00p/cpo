(defproject clj-photo-org "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 ; [image-resizer "0.1.10"]
                 [clj-exif "0.2"]
                 [clojure.java-time "0.3.2"]
                 [org.clojure/tools.cli "0.4.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [digest "1.4.9"]]
  :main ^:skip-aot clj-photo-org.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

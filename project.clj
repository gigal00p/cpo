(defproject clj-photo-org "0.0.8-SNAPSHOT"
  :description "CLI tool for organizing JPG photos"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clojure.java-time "0.3.3"]
                 [org.clojure/tools.cli "1.0.206"]
                 [com.taoensso/timbre "5.2.1"]
                 [digest "1.4.10"]
                 [eftest "0.5.9"]  ; testing library
                 [io.joshmiller/exif-processor "0.3.0"]]
  :main ^:skip-aot clj-photo-org.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

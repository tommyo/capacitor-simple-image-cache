import Foundation
import Capacitor

let KEY = "__capacitor_simple_image_cache__"
typealias JSObject = [String:Any]

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(ImageCache)
public class ImageCache: CAPPlugin {

    private var cache: NSDictionary?
    private var manager: SDWebImageManager?

    @objc func get(_ call: CAPPluginCall) {

        let src = call.getString("src") ?? ""

        if (!src.contains("http:") && !src.contains("https:")) {
            call.resolve([ "value": src ])
            return
        }

        let url = URL.init(string: src)

        self.manager?.loadImage(with: url, 
                                options: SDWebImageOptions.scaleDownLargeImages, 
                                progress: { (receivedSize, expectedSize, path) in

                                }, 
                                completed: { (image, data, error, type, finished, completedUrl) in

            if (image == nil && error != nil && data == nil) {
                call.reject(error!.localizedDescription)
                return
            }
            if (finished && completedUrl != nil) {
                let key = self.manager?.cacheKey(for: completedUrl)
                let source = self.manager?.imageCache?.defaultCachePath(forKey: key)
                let host = self.bridge!.getLocalUrl()

                let finalUrl = CAPFileManager.getPortablePath(host: host, uri: url)
                
                if (type == SDImageCacheType.disk) {
                    DispatchQueue.main.async {
                        call.resolve(["value": finalUrl])
                    }
                } else {
                    SDImageCache.shared().store(image, 
                                                forKey: completedUrl?.absoluteString, 
                                                completion: {
                        DispatchQueue.main.async {
                            call.resolve(["value": finalUrl])
                        }
                    })
                }
                return
            }
        })
    }

    @objc func hasItem(_ call: CAPPluginCall) {
        let src = call.getString("src") ?? ""
        let url = URL.init(string: src)
        manager?.cachedImageExists(for: url , completion: { (exists) in
            DispatchQueue.main.async {
                call.resolve(["value": exists])
            }
        })
    }

    @objc func clearItem(_ call: CAPPluginCall) {
        let src = call.getString("src") ?? ""
        manager?.imageCache?.removeImage(forKey: src, fromDisk: true, withCompletion: {
            DispatchQueue.main.async {
                call.resolve()
            }
        })

    }

    @objc func clear(_ call: CAPPluginCall) {
        manager?.imageCache?.clearMemory()
    }
}

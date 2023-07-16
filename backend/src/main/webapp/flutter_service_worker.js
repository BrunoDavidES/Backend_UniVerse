'use strict';
const MANIFEST = 'flutter-app-manifest';
const TEMP = 'flutter-temp-cache';
const CACHE_NAME = 'flutter-app-cache';
const RESOURCES = {
  "assets/AssetManifest.json": "69a0f2b40b79eb5feee1c7e5d9bd5b7e",
"assets/assets/app/logo_full_background.png": "aadbbedcedd03fb18856eeb3c93af6fd",
"assets/assets/app/logo_nova_horiz.png": "0588a7e44c9bdbd1e8b31057f20ed18d",
"assets/assets/app/logo_no_reference_no_white.png": "469c945d67a8fa36f1b54ec484971d03",
"assets/assets/app/profile_title.png": "73ba25f513aec0fad292000c4c4d202d",
"assets/assets/bill.jpg": "d5bd0a1db4d0a949858b546ea10e1463",
"assets/assets/capi.png": "05b34efb1be19c4e2c3774e268aa6cf4",
"assets/assets/images/404.png": "a3e5a1e4439f486d8ec5ed37dd3e5673",
"assets/assets/images/bigger_dot.png": "8c4a86da175b741fd6fdca7a8e189653",
"assets/assets/images/capi_logo.png": "7dbdc77f478443d7cd080957e97cded6",
"assets/assets/images/dot.png": "8a678b8eeefdea68708d975fba40915f",
"assets/assets/images/icon_no_white.png": "a7e8d8b8d2d895aec766ac3812658b0b",
"assets/assets/images/photo_1.jpg": "36780691fc752b3219d3c849bb67460f",
"assets/assets/images/photo_2.jpg": "b6180403fd17f1079f7ba2fed93384a9",
"assets/assets/images/photo_3.jpg": "aee698f87109f2509b5c0d8507ea380a",
"assets/assets/images/photo_4.jpg": "5afe2bd48925b308313061117d7f9b78",
"assets/assets/images/photo_5.jpg": "3747f9e0b830bbe44ebc9a58aa784c9a",
"assets/assets/images/photo_6.jpg": "061017fe8f2b520d8ea0e87917a31bde",
"assets/assets/images/photo_7.jpg": "1c4dd773d1485b93d8aaef2e54a28247",
"assets/assets/images/photo_8.jpg": "852436ee763d71931d9baaaadd454fe3",
"assets/assets/images/ufo.png": "c803e9c011bd9c165a266b46a11ad455",
"assets/assets/images/welcome_photo.jpg": "ec9c1879c03cd148cdde502df6de1b72",
"assets/assets/img.png": "6c1af842fdcd1608312518995b616fb1",
"assets/assets/locations/locations.json": "16e8046208203ffc6f34d00497a024d7",
"assets/assets/man.png": "7081f9bfac95cd1ae4a62d9e002bd449",
"assets/assets/titles/about_us.png": "7aa62483568dccadde5d4fcc2246429a",
"assets/assets/titles/agencies.png": "e5593020bea0e19e92d751d8f57d3d53",
"assets/assets/titles/area.png": "89e51ccc9201d5dec2a540c7d5fb1a06",
"assets/assets/titles/buildings.png": "6af709eca0ddf8726559d412cd64f155",
"assets/assets/titles/calendar.png": "de728e61a8c0673a043f831ddf14e479",
"assets/assets/titles/courses.png": "7af8fbdd9b4da6266f8c497b27c8b673",
"assets/assets/titles/departments.png": "bb00af7791a2ae5041c90c7eccb09b50",
"assets/assets/titles/edit.png": "4967d135792707ec23dca866dea8a91d",
"assets/assets/titles/events.png": "4ad0f5762e825e05539e9c0bb85c5309",
"assets/assets/titles/faq.png": "7193605ed3fb2b9716045c8e8d16ba94",
"assets/assets/titles/feed.png": "078becd5c095a14481dcdaf9182caf20",
"assets/assets/titles/feedback.png": "953d6c289fc18ed5af992869b7d7ce19",
"assets/assets/titles/find.png": "39eb33d0261043cf7528de843035adc2",
"assets/assets/titles/help.png": "6fa65d50ddc13f7d82b73e40c3908f32",
"assets/assets/titles/highlights.png": "c8c1d8c9ce6dac1f427b4602889e2379",
"assets/assets/titles/links.png": "e14dc80a7a0cb9575e172048f96952fe",
"assets/assets/titles/login.png": "77cb4fa9a041b545fed19a18e6b915c4",
"assets/assets/titles/map.png": "4127def0a33a5cc3d9c1468179248525",
"assets/assets/titles/messages.png": "8b80dda621994f5370d7cdc50de5b73c",
"assets/assets/titles/modify_pwd.png": "0698bc39229818c48d1b30697bbdfca1",
"assets/assets/titles/news.png": "f72203720d08e78260557f8b40518790",
"assets/assets/titles/organizations.png": "e6b6ae2f338e5ddfe54a7eb4f065e358",
"assets/assets/titles/organize.png": "51dc4f3ba325ae0c6e677d7ec463b6a7",
"assets/assets/titles/people.png": "843c5b3282e0e614fc8d6426abd5f686",
"assets/assets/titles/regist.png": "49cd97b9176fd3843d579153343781f1",
"assets/assets/titles/report.png": "5f10a132cf23ce51a96acfbe42d58912",
"assets/assets/titles/restaurants.png": "c7ba7d5da9dd4381acd946eff2b51485",
"assets/assets/titles/services.png": "74dc0539150f175d52e9587f58f32703",
"assets/assets/web/android.png": "84ed0cc712f918953624995c07dee23f",
"assets/assets/web/icon.ico": "5a74162b7f2db4171ac0ed1c23b51795",
"assets/assets/web/ios.png": "1be62602fbfd0b31d46c74424435690d",
"assets/assets/web/logo.png": "d9ad91466b5fdc7487c1b5b265961310",
"assets/assets/web/logoNova.png": "e50cc5e52f9a8b1ebb170d98f51d0f39",
"assets/assets/web/profile-title.png": "85629040d64370e33482eb8becfe0204",
"assets/assets/web/qr.png": "fcd30886010af37052858c0121e79b47",
"assets/FontManifest.json": "dc3d03800ccca4601324923c0b1d6d57",
"assets/fonts/MaterialIcons-Regular.otf": "e7069dfd19b331be16bed984668fe080",
"assets/NOTICES": "566e1295124f9bd52c6be76f628e9e2a",
"assets/packages/cupertino_icons/assets/CupertinoIcons.ttf": "6d342eb68f170c97609e9da345464e5e",
"canvaskit/canvaskit.js": "97937cb4c2c2073c968525a3e08c86a3",
"canvaskit/canvaskit.wasm": "3de12d898ec208a5f31362cc00f09b9e",
"canvaskit/profiling/canvaskit.js": "c21852696bc1cc82e8894d851c01921a",
"canvaskit/profiling/canvaskit.wasm": "371bc4e204443b0d5e774d64a046eb99",
"favicon.ico": "68ac2abdd2858565da7948302d533de9",
"flutter.js": "a85fcf6324d3c4d3ae3be1ae4931e9c5",
"icons/icon-192x192.png": "6b6f9c80f67e5213d5fcfb073c3b3c5c",
"icons/icon-512x512.png": "b1569551a0d662073476a37fd540eb02",
"index.html": "c7681be186498306fd84bb6c92014a1b",
"/": "c7681be186498306fd84bb6c92014a1b",
"logo.png": "a7e8d8b8d2d895aec766ac3812658b0b",
"main.dart.js": "f3b4612342dec7368921f67315940947",
"manifest.json": "67b0a8905cb2aef94551c387ce8f46a0",
"version.json": "b5c7ecde8d08ddcb76521c6253147174"
};

// The application shell files that are downloaded before a service worker can
// start.
const CORE = [
  "main.dart.js",
"index.html",
"assets/AssetManifest.json",
"assets/FontManifest.json"];
// During install, the TEMP cache is populated with the application shell files.
self.addEventListener("install", (event) => {
  self.skipWaiting();
  return event.waitUntil(
    caches.open(TEMP).then((cache) => {
      return cache.addAll(
        CORE.map((value) => new Request(value, {'cache': 'reload'})));
    })
  );
});

// During activate, the cache is populated with the temp files downloaded in
// install. If this service worker is upgrading from one with a saved
// MANIFEST, then use this to retain unchanged resource files.
self.addEventListener("activate", function(event) {
  return event.waitUntil(async function() {
    try {
      var contentCache = await caches.open(CACHE_NAME);
      var tempCache = await caches.open(TEMP);
      var manifestCache = await caches.open(MANIFEST);
      var manifest = await manifestCache.match('manifest');
      // When there is no prior manifest, clear the entire cache.
      if (!manifest) {
        await caches.delete(CACHE_NAME);
        contentCache = await caches.open(CACHE_NAME);
        for (var request of await tempCache.keys()) {
          var response = await tempCache.match(request);
          await contentCache.put(request, response);
        }
        await caches.delete(TEMP);
        // Save the manifest to make future upgrades efficient.
        await manifestCache.put('manifest', new Response(JSON.stringify(RESOURCES)));
        return;
      }
      var oldManifest = await manifest.json();
      var origin = self.location.origin;
      for (var request of await contentCache.keys()) {
        var key = request.url.substring(origin.length + 1);
        if (key == "") {
          key = "/";
        }
        // If a resource from the old manifest is not in the new cache, or if
        // the MD5 sum has changed, delete it. Otherwise the resource is left
        // in the cache and can be reused by the new service worker.
        if (!RESOURCES[key] || RESOURCES[key] != oldManifest[key]) {
          await contentCache.delete(request);
        }
      }
      // Populate the cache with the app shell TEMP files, potentially overwriting
      // cache files preserved above.
      for (var request of await tempCache.keys()) {
        var response = await tempCache.match(request);
        await contentCache.put(request, response);
      }
      await caches.delete(TEMP);
      // Save the manifest to make future upgrades efficient.
      await manifestCache.put('manifest', new Response(JSON.stringify(RESOURCES)));
      return;
    } catch (err) {
      // On an unhandled exception the state of the cache cannot be guaranteed.
      console.error('Failed to upgrade service worker: ' + err);
      await caches.delete(CACHE_NAME);
      await caches.delete(TEMP);
      await caches.delete(MANIFEST);
    }
  }());
});

// The fetch handler redirects requests for RESOURCE files to the service
// worker cache.
self.addEventListener("fetch", (event) => {
  if (event.request.method !== 'GET') {
    return;
  }
  var origin = self.location.origin;
  var key = event.request.url.substring(origin.length + 1);
  // Redirect URLs to the index.html
  if (key.indexOf('?v=') != -1) {
    key = key.split('?v=')[0];
  }
  if (event.request.url == origin || event.request.url.startsWith(origin + '/#') || key == '') {
    key = '/';
  }
  // If the URL is not the RESOURCE list then return to signal that the
  // browser should take over.
  if (!RESOURCES[key]) {
    return;
  }
  // If the URL is the index.html, perform an online-first request.
  if (key == '/') {
    return onlineFirst(event);
  }
  event.respondWith(caches.open(CACHE_NAME)
    .then((cache) =>  {
      return cache.match(event.request).then((response) => {
        // Either respond with the cached resource, or perform a fetch and
        // lazily populate the cache only if the resource was successfully fetched.
        return response || fetch(event.request).then((response) => {
          if (response && Boolean(response.ok)) {
            cache.put(event.request, response.clone());
          }
          return response;
        });
      })
    })
  );
});

self.addEventListener('message', (event) => {
  // SkipWaiting can be used to immediately activate a waiting service worker.
  // This will also require a page refresh triggered by the main worker.
  if (event.data === 'skipWaiting') {
    self.skipWaiting();
    return;
  }
  if (event.data === 'downloadOffline') {
    downloadOffline();
    return;
  }
});

// Download offline will check the RESOURCES for all files not in the cache
// and populate them.
async function downloadOffline() {
  var resources = [];
  var contentCache = await caches.open(CACHE_NAME);
  var currentContent = {};
  for (var request of await contentCache.keys()) {
    var key = request.url.substring(origin.length + 1);
    if (key == "") {
      key = "/";
    }
    currentContent[key] = true;
  }
  for (var resourceKey of Object.keys(RESOURCES)) {
    if (!currentContent[resourceKey]) {
      resources.push(resourceKey);
    }
  }
  return contentCache.addAll(resources);
}

// Attempt to download the resource online before falling back to
// the offline cache.
function onlineFirst(event) {
  return event.respondWith(
    fetch(event.request).then((response) => {
      return caches.open(CACHE_NAME).then((cache) => {
        cache.put(event.request, response.clone());
        return response;
      });
    }).catch((error) => {
      return caches.open(CACHE_NAME).then((cache) => {
        return cache.match(event.request).then((response) => {
          if (response != null) {
            return response;
          }
          throw error;
        });
      });
    })
  );
}

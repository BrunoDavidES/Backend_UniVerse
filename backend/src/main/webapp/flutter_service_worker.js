'use strict';
const MANIFEST = 'flutter-app-manifest';
const TEMP = 'flutter-temp-cache';
const CACHE_NAME = 'flutter-app-cache';

const RESOURCES = {"version.json": "b5c7ecde8d08ddcb76521c6253147174",
"index.html": "e3c25f091ffc5a8ddeede40fe8bcbcb2",
"/": "e3c25f091ffc5a8ddeede40fe8bcbcb2",
"main.dart.js": "0e089b866d719e5c053f73f4be249aba",
"flutter.js": "6fef97aeca90b426343ba6c5c9dc5d4a",
"favicon.png": "5dcef449791fa27946b3d35ad8803796",
"logo.png": "a7e8d8b8d2d895aec766ac3812658b0b",
"icons/Icon-192.png": "ac9a721a12bbc803b44f645561ecb1e1",
"icons/Icon-maskable-192.png": "c457ef57daa1d16f64b27b786ec2ea3c",
"icons/Icon-maskable-512.png": "301a7604d45b3e739efc881eb04896ea",
"icons/Icon-512.png": "96e752610906ba2a93c65f8abe1645f1",
"manifest.json": "c73a107ed491b074b64a351ff6a007b9",
"assets/AssetManifest.json": "9deca4880880095b838dd3670198ce45",
"assets/NOTICES": "a37b20882cd1744b716cdab6f622d4ab",
"assets/FontManifest.json": "dc3d03800ccca4601324923c0b1d6d57",
"assets/packages/cupertino_icons/assets/CupertinoIcons.ttf": "57d849d738900cfd590e9adc7e208250",
"assets/shaders/ink_sparkle.frag": "f8b80e740d33eb157090be4e995febdf",
"assets/AssetManifest.bin": "badf6beb89e6968ed4d2a9ea41f823a2",
"assets/fonts/MaterialIcons-Regular.otf": "dde8e910c7b21b3dbe9a034a9b555954",
"assets/assets/icon_no_white.png": "a7e8d8b8d2d895aec766ac3812658b0b",
"assets/assets/app/area.png": "f5c78a0a399db7946f4fd02fe6562da2",
"assets/assets/app/feed_title.png": "301e8e1b5cf3d54c5ebbecd6c1ab8a76",
"assets/assets/app/faq_title.png": "27bda88ae8660a92541c79a8d23bb73f",
"assets/assets/app/logo_no_reference_no_white.png": "469c945d67a8fa36f1b54ec484971d03",
"assets/assets/app/login.png": "c086f770d63076391bb4ddf67fe72e05",
"assets/assets/app/dot.png": "8a678b8eeefdea68708d975fba40915f",
"assets/assets/app/services.png": "74dc0539150f175d52e9587f58f32703",
"assets/assets/app/map.png": "4127def0a33a5cc3d9c1468179248525",
"assets/assets/app/ajuda.png": "6fa65d50ddc13f7d82b73e40c3908f32",
"assets/assets/app/about_us.png": "e0edf7a46dd284fdf9e00db76cfd9764",
"assets/assets/app/registo.png": "a7cbe7696705fea3292c54439ab1ae13",
"assets/assets/app/welcome_app.jpg": "abe0a9763424a69272264bb88f55b140",
"assets/assets/app/departments.png": "bb00af7791a2ae5041c90c7eccb09b50",
"assets/assets/app/links.png": "e14dc80a7a0cb9575e172048f96952fe",
"assets/assets/app/profile_title.png": "73ba25f513aec0fad292000c4c4d202d",
"assets/assets/app/logo_full_background.png": "aadbbedcedd03fb18856eeb3c93af6fd",
"assets/assets/app/logo_nova_horiz.png": "0588a7e44c9bdbd1e8b31057f20ed18d",
"assets/assets/app/find_title.png": "8ea539e7f287513d19ef3051415bbdc6",
"assets/assets/app/report.png": "5f10a132cf23ce51a96acfbe42d58912",
"assets/assets/capi.png": "05b34efb1be19c4e2c3774e268aa6cf4",
"assets/assets/images/ufo.png": "c803e9c011bd9c165a266b46a11ad455",
"assets/assets/web/area.png": "f6524d15bed158df646b7519bcffb47d",
"assets/assets/web/logoNova.png": "e50cc5e52f9a8b1ebb170d98f51d0f39",
"assets/assets/web/faq_title.png": "a76e31e7de654928c45679fbabdfe59e",
"assets/assets/web/404.png": "a3e5a1e4439f486d8ec5ed37dd3e5673",
"assets/assets/web/sobre.png": "d2d064dca0d1dbfb74472b23f80cb82e",
"assets/assets/web/perfil-web.png": "620550c12b9dddb3dea600d4f3466dcb",
"assets/assets/web/logoNovaBranco.png": "3ab9c2a0e2df7d1835185e880d99148b",
"assets/assets/web/android.png": "84ed0cc712f918953624995c07dee23f",
"assets/assets/web/bigger_dot.png": "4e3bfc9d2b6f9487b38bfbabcb5447a0",
"assets/assets/web/destaques.png": "25a3ab95d5edadc2f5b690ee71aa93b3",
"assets/assets/web/BackGround.jpg": "a366dc52456690e028f9f9ec85922e4c",
"assets/assets/web/icon.ico": "5a74162b7f2db4171ac0ed1c23b51795",
"assets/assets/web/help.png": "4c6717c45ffe7065687f968a7094d416",
"assets/assets/web/foto.jpg": "05618f0c9dc571a999b283621ca23323",
"assets/assets/web/logo.png": "d9ad91466b5fdc7487c1b5b265961310",
"assets/assets/web/ios.png": "1be62602fbfd0b31d46c74424435690d",
"assets/assets/web/findTitle.jpeg": "5749bc14b1d56d9e38255d5f4b46ba17",
"assets/assets/web/combo_logo.png": "2150ef53d9d5e28ca477e078d61c6f1f",
"assets/assets/web/FCT-NOVA.jpg": "a00e660229c7ef706ed4276e6a56f4df",
"assets/assets/web/noticias.png": "f72203720d08e78260557f8b40518790",
"assets/assets/web/Example.png": "fcd30886010af37052858c0121e79b47",
"assets/assets/capi_logo.png": "7dbdc77f478443d7cd080957e97cded6",
"assets/assets/titles/events.png": "4ad0f5762e825e05539e9c0bb85c5309",
"assets/assets/titles/calendar.png": "de728e61a8c0673a043f831ddf14e479",
"assets/assets/titles/messages.png": "0efb3c6a3f6d5faa83b603297f155a5c",
"assets/assets/man.png": "7081f9bfac95cd1ae4a62d9e002bd449",
"assets/assets/locations/locations.json": "b37141b37eaa28a30f7b9a01da6f5e45",
"canvaskit/skwasm.js": "1df4d741f441fa1a4d10530ced463ef8",
"canvaskit/skwasm.wasm": "6711032e17bf49924b2b001cef0d3ea3",
"canvaskit/chromium/canvaskit.js": "8c8392ce4a4364cbb240aa09b5652e05",
"canvaskit/chromium/canvaskit.wasm": "fc18c3010856029414b70cae1afc5cd9",
"canvaskit/canvaskit.js": "76f7d822f42397160c5dfc69cbc9b2de",
"canvaskit/canvaskit.wasm": "f48eaf57cada79163ec6dec7929486ea",
"canvaskit/skwasm.worker.js": "19659053a277272607529ef87acf9d8a"};
// The application shell files that are downloaded before a service worker can
// start.
const CORE = ["main.dart.js",
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
        // Claim client to enable caching on first launch
        self.clients.claim();
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
      // Claim client to enable caching on first launch
      self.clients.claim();
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

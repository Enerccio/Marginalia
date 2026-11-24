// The page of the API (ApiDocs): Swagger UI over openapi.json. The key is kept in this variable only, never in storage: it is sent
// with the description (which then lists the commands of the key) and with every call of "Try it out".
(function () {
    'use strict';
    var key = null;
    // the page is /<context>/api/v1/, the calls go to /<context>/api/v1/...
    var base = location.pathname.replace(/\/+$/, '');
    var state = document.getElementById('state');

    function show(text, error) {
        state.textContent = text || '';
        state.className = error ? 'error' : '';
    }

    function load() {
        var headers = key ? {Authorization: 'Bearer ' + key} : {};
        return fetch(base + '/openapi.json', {headers: headers, cache: 'no-store', credentials: 'omit'})
            .then(function (response) {
                if (response.status === 401 && key) {
                    key = null;
                    show('The key is not valid or has expired.', true);
                    return load();
                }
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.json().then(render);
            })
            .catch(function (e) {
                show('The description of the API could not be read: ' + e.message, true);
            });
    }

    function render(spec) {
        spec.servers = [{url: location.origin + base}];
        SwaggerUIBundle({
            spec: spec,
            dom_id: '#swagger-ui',
            deepLinking: true,
            persistAuthorization: false,
            validatorUrl: null,
            displayRequestDuration: true,
            docExpansion: 'none',
            filter: true,
            requestInterceptor: function (request) {
                // the key of the page, unless the Authorize button of Swagger UI gave one
                if (key && !request.headers.Authorization) {
                    request.headers.Authorization = 'Bearer ' + key;
                }
                request.credentials = 'omit';
                return request;
            }
        });
        if (key) {
            var commands = Object.keys(spec.paths).filter(function (p) {
                return /^\/books\/\{book}\/commands\/[^{]/.test(p);
            }).length;
            show('Using the key: ' + commands + ' commands.', false);
        }
    }

    document.getElementById('api-key').addEventListener('submit', function (event) {
        event.preventDefault();
        var input = document.getElementById('key');
        var typed = input.value.trim();
        input.value = '';
        key = typed || null;
        show(key ? 'Reading the commands of the key…' : '', false);
        load();
    });
    document.getElementById('forget').addEventListener('click', function () {
        key = null;
        show('', false);
        load();
    });
    load();
})();

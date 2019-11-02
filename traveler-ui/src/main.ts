import App from "./App.vue";
import router from "./router";
import store from "./store";
import {Vue} from "vue-property-decorator";
import "leaflet/dist/leaflet.css";

new Vue({
    router,
    store,
    render: h => h(App)
}).$mount("#app");

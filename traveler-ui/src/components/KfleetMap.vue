<template>
    <l-map :center="center" :zoom="zoom" style="height: 100%; width: 600px; margin:auto;">
        <l-tile-layer :url="url"></l-tile-layer>
        <l-rectangle
                :bounds="rectangle.bounds"
                :l-style="rectangle.style">
        </l-rectangle>
        <l-rectangle
                :bounds="bounds"
                :key="index"
                :l-style="style" v-for="(bounds, index) in quadTreeBounds"></l-rectangle>
        <l-rectangle
                :bounds="area.bounds"
                :l-style="area.style">
        </l-rectangle>
    </l-map>
</template>

<script lang="ts">
    import 'leaflet'
    import {LMap, LRectangle, LTileLayer} from "vue2-leaflet";
    import {Vue} from "vue-property-decorator";

    Vue.component("l-map", LMap);
    Vue.component("l-tile-layer", LTileLayer);
    Vue.component('l-rectangle', LRectangle);

    export default {
        data() {
            return {
                url: "http://{s}.tile.osm.org/{z}/{x}/{y}.png",
                zoom: 10,
                center: [59.9, 10.7],
                rectangle: {
                    bounds: [[59.7984951859, 10.3772388202], [60.0334203198, 10.9805373651]],
                    style: {color: 'blue', weight: 1}
                },
                area: {
                    bounds: [[59.89089997749998, 10.563917402606574], [59.93590002250002, 10.653682597393425]],
                    style: {color: 'red', weight: 1}
                },
                quadTreeBounds: [
                    [[59.8974609375, 10.546875], [59.94140625, 10.634765625]],
                    [[59.8974609375, 10.634765625], [59.94140625, 10.72265625]],
                    [[59.853515625, 10.634765625], [59.8974609375, 10.72265625]],
                    [[59.853515625, 10.546875], [59.8974609375, 10.634765625]]
                ],
                style: {color: 'yellow', weight: 1}
            };
        },
        methods: {}
    };
</script>

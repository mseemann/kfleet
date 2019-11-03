<template>
    <l-map :center="center" :zoom="zoom" style="height: 100%; width: 600px; margin:auto;">
        <l-tile-layer :url="url"></l-tile-layer>
        <l-rectangle
                :bounds="rectangle.bounds"
                :l-style="rectangle.style">
        </l-rectangle>
        <!--l-rectangle
                :bounds="bounds"
                :key="index"
                :l-style="rectangle.style" v-for="(bounds, index) in quadTreeBounds"></l-rectangle-->
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
                    bounds: [[59.86839995499996, 10.58903480521315], [59.95840004500005, 10.768565194786854]],
                    style: {color: 'red', weight: 1}
                },
                quadTreeBounds: [
                    [[-90.0, -180.0], [0.0, 0.0]],
                    [[-45.0, -90.0], [0.0, 0.0]],
                    [[-45.0, -90.0], [-22.5, -45.0]],
                    [[-33.75, -67.5], [-22.5, -45.0]],
                    [[-28.125, -56.25], [-22.5, -45.0]],
                    [[-25.3125, -50.625], [-22.5, -45.0]],
                    [[-23.90625, -47.8125], [-22.5, -45.0]],
                    [[-23.90625, -47.8125], [-23.203125, -46.40625]],
                    [[-23.5546875, -47.109375], [-23.203125, -46.40625]],
                    [[-23.5546875, -46.7578125], [-23.37890625, -46.40625]],
                    [[-23.5546875, -46.7578125], [-23.466796875, -46.58203125]],
                    [[0.0, 0.0], [90.0, 180.0]],
                    [[45.0, 0.0], [90.0, 90.0]],
                    [[45.0, 0.0], [67.5, 45.0]],
                    [[56.25, 0.0], [67.5, 22.5]],
                    [[56.25, 0.0], [61.875, 11.25]],
                    [[59.0625, 5.625], [61.875, 11.25]],
                    [[59.0625, 8.4375], [60.46875, 11.25]],
                    [[59.765625, 9.84375], [60.46875, 11.25]],
                    [[59.765625, 10.546875], [60.1171875, 11.25]],
                    [[59.94140625, 10.8984375], [60.1171875, 11.25]],
                    [[60.029296875, 10.8984375], [60.1171875, 11.07421875]],
                    [[60.029296875, 10.8984375], [60.0732421875, 10.986328125]]
                ]
            };
        },
        methods: {}
    };
</script>

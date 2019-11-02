<template>
    <div class="home" style="height: 500px">
        <kfleet-map></kfleet-map>
    </div>
</template>

<script>
    // @ is an alias to /src
    import KfleetMap from "@/components/KfleetMap";

    export default {
        name: "home",
        components: {KfleetMap}
    };
</script>

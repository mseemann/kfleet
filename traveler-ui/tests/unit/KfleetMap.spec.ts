import {shallowMount} from "@vue/test-utils";
import KfleetMap from '@/components/KfleetMap.vue';

describe("KfleetMap.vue", () => {
    it("should create the component", () => {
        const wrapper = shallowMount(KfleetMap);
        expect(wrapper.isVueInstance());
    });
});

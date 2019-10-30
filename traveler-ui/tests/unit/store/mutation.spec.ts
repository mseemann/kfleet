import {expect} from 'chai'

// destructure assign `mutations`

describe('mutations', () => {
    it('INCREMENT', () => {
        // mock state
        const state = {count: 0}
        // apply mutation

        // assert result
        expect(state.count).to.equal(0)
    })
})

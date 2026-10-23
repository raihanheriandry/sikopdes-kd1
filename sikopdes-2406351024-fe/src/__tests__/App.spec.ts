import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '../App.vue'
import router from '../router'

describe('App', () => {
  it('mounts and renders SIKopDes navigation bar properly', async () => {
    const wrapper = mount(App, {
      global: {
        plugins: [router],
      },
    })
    expect(wrapper.text()).toContain('SIKopDes')
    expect(wrapper.text()).toContain('KD1: Keanggotaan & Simpanan')
  })
})

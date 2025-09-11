import {describe, it} from 'vitest';

describe('Refinement', () => {
  it('assign undefined ', () => {
    const dto: {
      readonly domain?: {
        readonly tag: number
      }
    } = {domain: undefined}
    const domain = dto.domain?.tag
    console.log(domain)
  })
});

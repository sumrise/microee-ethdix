
import { ChainId, Currency, currencyEquals, JSBI, Price, WETH, Token, ETHER, Pair } from '@uniswap/sdk'

export function wrappedCurrency(currency: Currency | undefined, chainId: ChainId | undefined): Token | undefined {
    return chainId && currency === ETHER ? WETH[chainId] : currency instanceof Token ? currency : undefined
}
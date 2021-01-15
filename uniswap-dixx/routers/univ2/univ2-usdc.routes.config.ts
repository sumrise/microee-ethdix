import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import { expect } from 'chai';
import web3 from 'web3';
import { ChainId, WETH, Token, Pair, JSBI, Price } from '@uniswap/sdk'
import { getPairDataWithSymbol, getPairSymbolByAddress, getTokenObjectByAddress } from '../../univ2-resolves/UniV2Fetcher';

const USDC: Token = new Token(ChainId.MAINNET, '0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48', 6, 'USDC', 'USD Coin');

// 不能查 usdc, 不能查 weth, 只能查主网
// token/weth, token/usdc, weth/usdc
async function wrappedPairs(token: Token): Promise<[Pair, string][]> {
    expect(token.address.toLowerCase() !== USDC.address.toLowerCase(), '不能查usdc').to.be.true;
    expect(token.address.toLowerCase() !== WETH[ChainId.MAINNET].address.toLowerCase(), '不能查weth').to.be.true;
    const [tokenWETHPair, tokenWETHSymbol] = await getPairDataWithSymbol(ChainId.MAINNET, token, WETH[ChainId.MAINNET]);
    const [tokenUSDCPair, tokenUSDCSymbol] = await getPairDataWithSymbol(ChainId.MAINNET, token, USDC);
    const [WETHUSDCPair, WETHUSDCSymbol] = await getPairDataWithSymbol(ChainId.MAINNET, WETH[ChainId.MAINNET], USDC);
    return [[tokenWETHPair, tokenWETHSymbol], [tokenUSDCPair, tokenUSDCSymbol], [WETHUSDCPair, WETHUSDCSymbol]];
}

export class UniV2USDCRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'UniV2USDCRoutes');
    }
    configureRoutes() {
        // token/weth, token/usdc, weth/usdc
        this.app.route(`/univ2-sdk/usdcPrice`)
            .post((req: express.Request, res: express.Response, next: express.NextFunction) => {
                //const wethCurrency: Currency = WETH; 
                //const uniToken Token = new Token(ChainId.MAINNET, '0x1f9840a85d5af5bf1d1762f925bdaddc4201f984', 18, 'UNI', 'Uniswap');
                //const univ2Token: Token = new Token(ChainId.MAINNET, '0xd4405f0704621dbe9d4dea60e128e0c3b26bddbd', 18, 'UNI-V2', 'Uniswap V2'); // 不支持
                //const daiToken: Token = new Token(ChainId.MAINNET, '0x6B175474E89094C44Da98b954EedeAC495271d0F', 18, 'DAI', 'Dai Stablecoin');
                //const daiToken: Token = new Token(ChainId.MAINNET, '0x2260fac5e5542a773aa44fbcfedf7c193bc2c599', 8, 'WBTC', 'Wrapped BTC');
                //const daiToken: Token = new Token(ChainId.MAINNET, '0xdf574c24545e5ffecb9a659c229253d4111d87e1', 8, 'HUSD', 'HUSD');
                //const daiToken: Token = new Token(ChainId.MAINNET, '0xdAC17F958D2ee523a2206206994597C13D831ec7', 6, 'USDT', 'Tether USD');
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _params: any = Object.assign({ chainId: _chainId, address: null, decimals: null, symbol: null, name: null }, req.body);
                expect(web3.utils.isAddress(_params.address), '`address`无效').to.be.true;
                if (_params.decimals) {
                    expect(_params.decimals, '`decimals`无效').to.be.gt(0);
                } else {
                    _params.decimals = getTokenObjectByAddress(ChainId.MAINNET, _params.address).decimals;
                }
                function func(price: string) {
                    if (price.indexOf('.') === -1) {
                        const str: string = price.split("").reverse().join("");
                        const str2: string = str.substr(0, 10) + '.' + str.substr(10);
                        const str3: string = str2.split("").reverse().join("");
                        price = str3;
                    }
                    return price.replace(/0+$/g, '');
                }
                (async () => {
                    try {
                        const _token = new Token(_params.chainId, _params.address, _params.decimals, _params.symbol, _params.name);
                        const [[tokenWETHPair, tokenWETHSymbol], [tokenUSDCPair, tokenUSDCSymbol], [WETHUSDCPair, WETHUSDCSymbol]] = await wrappedPairs(_token);
                        const ethPairETHAmount = tokenWETHPair.reserveOf(WETH[ChainId.MAINNET]);
                        const ethPairETHUSDCValue: JSBI = WETHUSDCPair.priceOf(WETH[ChainId.MAINNET]).quote(ethPairETHAmount).raw;
                        const rtnResult: any = { code: 200, message: null };
                        rtnResult.message = `[['${tokenWETHSymbol}'],['${tokenUSDCSymbol}'],['${WETHUSDCSymbol}']]`;
                        if (tokenUSDCPair.reserveOf(USDC).greaterThan(ethPairETHUSDCValue)) {
                            const _price = tokenUSDCPair.priceOf(_token);
                            const _thePrice = new Price(_token, USDC, _price.denominator, _price.numerator);
                            const baseCurrencyObject: any = JSON.parse(JSON.stringify(_thePrice.baseCurrency));
                            const quoteCurrencyObject: any = JSON.parse(JSON.stringify(_thePrice.quoteCurrency));
                            const [baseSymbol, quoteSymbol] = getPairSymbolByAddress(_chainId, baseCurrencyObject.address, quoteCurrencyObject.address);
                            rtnResult.data = func(_thePrice.toSignificant(6));
                            rtnResult.message = `${baseSymbol}/${quoteSymbol}`;
                        }
                        if (WETHUSDCPair.reserveOf(USDC).greaterThan('0') && tokenWETHPair.reserveOf(WETH[ChainId.MAINNET]).greaterThan('0')) {
                            const WETHUSDCPrice = WETHUSDCPair.priceOf(USDC);
                            const tokenWETHPrice = tokenWETHPair.priceOf(WETH[ChainId.MAINNET]);
                            const usdcPrice = WETHUSDCPrice.multiply(tokenWETHPrice).invert();
                            const _thePrice = new Price(_token, USDC, usdcPrice.denominator, usdcPrice.numerator);
                            const baseCurrencyObject: any = JSON.parse(JSON.stringify(_thePrice.baseCurrency));
                            const quoteCurrencyObject: any = JSON.parse(JSON.stringify(_thePrice.quoteCurrency));
                            const [baseSymbol, quoteSymbol] = getPairSymbolByAddress(_chainId, baseCurrencyObject.address, quoteCurrencyObject.address);
                            rtnResult.data = func(_thePrice.toSignificant(6));
                            rtnResult.message = `${baseSymbol}/${quoteSymbol}`;
                        }
                        res.status(200).json(rtnResult);
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        return this.app;
    }
}
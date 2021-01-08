import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import debug from 'debug';
import { ChainId, Token, WETH, Fetcher, Route } from '@uniswap/sdk'

const debugLog: debug.IDebugger = debug('app-univ2-simple');

// https://www.youtube.com/watch?v=0Im5iaYoz1Y
// https://uniswap.org/docs/v2/SDK/getting-started
export class UniV2SimplesRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'UniV2SimplesRoutes');
    }
    configureRoutes() {
        this.app.route(`/univ2-simple`)
            .get((req: express.Request, res: express.Response) => {
                debugLog(`The chainId of mainnet is ${ChainId.MAINNET}.`)
                res.status(200).send(`This is UniswapV2 Page, ChainId: ${ChainId.MAINNET}`);
            });
        this.app.route(`/univ2-simple/getETH2TokenRoute`)
            .get((req: express.Request, res: express.Response) => {
                const _tokenAddr = '0x6B175474E89094C44Da98b954EedeAC495271d0F'; // 代币地址
                const _chainId = ChainId.MAINNET;
                (async () => {
                    const token = await Fetcher.fetchTokenData(_chainId, _tokenAddr); // 根据代币地址获取erc20合约信息
                    const pair = await Fetcher.fetchPairData(token, WETH[_chainId]);
                    const route = new Route([pair], WETH[_chainId]);
                    debugLog(`pairAddress: ${pair.reserve0.currency}`);
                    debugLog(`WETH/DAI: ${route.midPrice.toSignificant(6)}`); // 201.306
                    debugLog(`DAI/WETH: ${route.midPrice.invert().toSignificant(6)}`); // 0.00496756
                    res.status(200).send(`This is UniswapV2 Page, ChainId: ${_chainId}`);
                })();
            });
        return this.app;
    }
}
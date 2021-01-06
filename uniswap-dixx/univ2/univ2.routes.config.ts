import { CommonRoutesConfig } from '../common/common.routes.config';
import express from 'express';
import debug from 'debug';
import { ChainId, Token, WETH, Fetcher, Route } from '@uniswap/sdk'

const debugLog: debug.IDebugger = debug('app-pricing');

export class UniV2sRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'UniV2sRoutes');
    }
    configureRoutes() {
        this.app.route(`/univ2`)
            .get((req: express.Request, res: express.Response) => {
                debugLog(`The chainId of mainnet is ${ChainId.MAINNET}.`)
                res.status(200).send(`This is UniswapV2 Page, ChainId: ${ChainId.MAINNET}`);
            });
        this.app.route(`/pricing`)
            .get((req: express.Request, res: express.Response) => {
                const DAI = new Token(ChainId.MAINNET, '0x6B175474E89094C44Da98b954EedeAC495271d0F', 18);
                (async () => {
                    const pair = await Fetcher.fetchPairData(DAI, WETH[DAI.chainId]);
                    const route = new Route([pair], WETH[DAI.chainId]);
                    debugLog(`pairAddress: ${pair.reserve0.currency}`);
                    debugLog(`WETH/DAI: ${route.midPrice.toSignificant(6)}`); // 201.306
                    debugLog(`DAI/WETH: ${route.midPrice.invert().toSignificant(6)}`); // 0.00496756
                    res.status(200).send(`This is UniswapV2 Page, ChainId: ${ChainId.MAINNET}`);
                })();
            });
        return this.app;
    }
}
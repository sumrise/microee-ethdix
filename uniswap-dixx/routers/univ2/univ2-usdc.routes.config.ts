import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import debug from 'debug';
import { expect } from 'chai';
import web3 from 'web3';

import { abi as IUniswapV2PairABI } from '@uniswap/v2-core/build/IUniswapV2Pair.json';
import { Interface } from '@ethersproject/abi';
import { ChainId, Currency, currencyEquals, JSBI, Price, WETH, Token, ETHER, Pair } from '@uniswap/sdk'
import { isAddress } from 'ethers/lib/utils';

const loggerInfo: debug.IDebugger = debug('app-univ2-usdc');

const PAIR_INTERFACE = new Interface(IUniswapV2PairABI)

function wrappedCurrency(currency: Currency | undefined, chainId: ChainId | undefined): Token | undefined {
    return chainId && currency === ETHER ? WETH[chainId] : currency instanceof Token ? currency : undefined
}

export class UniV2USDCRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'UniV2USDCRoutes');
    }
    configureRoutes() {
        this.app.route(`/univ2-sdk/usdcPrice`)
            .post((req: express.Request, res: express.Response) => {
                //const wethCurrency: Currency = WETH; 
                //const uniCurrency: Currency = new Token(ChainId.MAINNET, '0x1f9840a85d5af5bf1d1762f925bdaddc4201f984', 18, 'UNI', 'Uniswap');
                //const univ2Currency: Currency = new Token(ChainId.MAINNET, '0xd4405f0704621dbe9d4dea60e128e0c3b26bddbd', 18, 'UNI-V2', 'Uniswap V2');
                //const daiCurrency: Currency = new Token(ChainId.MAINNET, '0x6B175474E89094C44Da98b954EedeAC495271d0F', 18, 'DAI', 'Dai Stablecoin');
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _params: any = Object.assign({chainId: _chainId, address: null, decimals: null, symbol: null, name: null}, req.body);
                expect(web3.utils.isAddress(_params.address), 'address 无效').to.be.true;
                expect(_params.decimals, 'decimals 无效').to.be.gt(0);
                expect(_params.symbol, 'symbol 无效').to.be.length.gt(1);
                expect(_params.name, 'name 无效').to.be.length.gt(1);
                const USDC: Token = new Token(_chainId, '0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48', 6, 'USDC', 'USD Coin');
                const usdtCurrency: Currency = new Token(_params.chainId, _params.address, _params.decimals, _params.symbol, _params.name);
                const currency: Currency = usdtCurrency;
                const wrapped = wrappedCurrency(currency, _chainId);
                const tokenPairs: [Currency | undefined, Currency | undefined][] = [
                    [_chainId && wrapped && currencyEquals(WETH[_chainId], wrapped) ? undefined : currency, _chainId ? WETH[_chainId] : undefined],
                    [wrapped?.equals(USDC) ? undefined : wrapped, _chainId === ChainId.MAINNET ? USDC : undefined],
                    [_chainId ? WETH[_chainId] : undefined, _chainId === ChainId.MAINNET ? USDC : undefined]];
                const tokens = tokenPairs.map(([currencyA, currencyB]) => [wrappedCurrency(currencyA, _chainId), wrappedCurrency(currencyB, _chainId)]);
                const pairAddresses = tokens.map(([tokenA, tokenB]) => tokenA && tokenB && !tokenA.equals(tokenB) ? Pair.getAddress(tokenA, tokenB) : undefined);
                //const results = useMultipleContractSingleData(pairAddresses, PAIR_INTERFACE, 'getReserves');
                res.status(200).json({ code: 200, message: 'OK', data: pairAddresses });
            });
        return this.app;
    }
}
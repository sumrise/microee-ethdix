import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import debug from 'debug';
import { expect } from 'chai';
import web3 from 'web3';
import { BigNumber } from '@ethersproject/bignumber';
import { Interface, FunctionFragment } from '@ethersproject/abi'
import { abi as IUniswapV2PairABI } from '@uniswap/v2-core/build/IUniswapV2Pair.json';
import { ChainId, Currency, currencyEquals, JSBI, Price, WETH, Token, ETHER, Pair, Fetcher } from '@uniswap/sdk'

import { wrappedCurrency } from '../../univ2-resolves/UniV2Resolve';

type MethodArg = string | number | BigNumber;
type MethodArgs = Array<MethodArg | MethodArg[]>;
type OptionalMethodInputs = Array<MethodArg | MethodArg[] | undefined> | undefined

const loggerInfo: debug.IDebugger = debug('app-univ2-usdc');
const PAIR_INTERFACE = new Interface(IUniswapV2PairABI)


function wrappedPairs(chainId: ChainId, currencies: [Currency | undefined, Currency | undefined][]) {
    const tokens = currencies.map(([currencyA, currencyB]) => [wrappedCurrency(currencyA, chainId), wrappedCurrency(currencyB, chainId)]);
    const pairAddresses = tokens.map(([tokenA, tokenB]) => tokenA && tokenB && !tokenA.equals(tokenB) ? Pair.getAddress(tokenA, tokenB) : undefined);
    
    // const thePair = await Fetcher.fetchPairData(tokenA, tokenB, getDefaultProvider(getNetwork(tokenA.chainId)));
    // const results = useMultipleContractSingleData(pairAddresses, PAIR_INTERFACE, 'getReserves');
    return pairAddresses;
}


export class UniV2USDCRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'UniV2USDCRoutes');
    }
    configureRoutes() {
        // token/weth, token/usdc, weth/usdc
        this.app.route(`/univ2-sdk/usdcPrice`)
            .post((req: express.Request, res: express.Response) => {
                //const wethCurrency: Currency = WETH; 
                //const uniCurrency: Currency = new Token(ChainId.MAINNET, '0x1f9840a85d5af5bf1d1762f925bdaddc4201f984', 18, 'UNI', 'Uniswap');
                //const univ2Currency: Currency = new Token(ChainId.MAINNET, '0xd4405f0704621dbe9d4dea60e128e0c3b26bddbd', 18, 'UNI-V2', 'Uniswap V2');
                //const daiCurrency: Currency = new Token(ChainId.MAINNET, '0x6B175474E89094C44Da98b954EedeAC495271d0F', 18, 'DAI', 'Dai Stablecoin');
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _params: any = Object.assign({ chainId: _chainId, address: null, decimals: null, symbol: null, name: null }, req.body);
                expect(web3.utils.isAddress(_params.address), 'address 无效').to.be.true;
                expect(_params.decimals, 'decimals 无效').to.be.gt(0);
                expect(_params.symbol, 'symbol 无效').to.be.length.gt(1);
                expect(_params.name, 'name 无效').to.be.length.gt(1);
                const USDC: Token = new Token(_chainId, '0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48', 6, 'USDC', 'USD Coin');
                const usdtCurrency: Currency = new Token(_params.chainId, _params.address, _params.decimals, _params.symbol, _params.name);
                const currency: Currency = usdtCurrency;
                const wrapped = wrappedCurrency(currency, _chainId);
                const currencies: [Currency | undefined, Currency | undefined][] = [
                    [wrapped && currencyEquals(WETH[_chainId], wrapped) ? undefined : currency, WETH[_chainId]],
                    [wrapped?.equals(USDC) ? undefined : wrapped, USDC], [WETH[_chainId], USDC]];
                const results = wrappedPairs(_chainId, currencies);
                res.status(200).json({ code: 200, message: 'OK', data: results });
            });
        return this.app;
    }
}
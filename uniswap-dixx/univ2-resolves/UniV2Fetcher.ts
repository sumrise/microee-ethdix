import { ChainId, Token, Fetcher, Pair } from '@uniswap/sdk';
import { tokens as DefaultTokenList } from '@uniswap/default-token-list/build/uniswap-default.tokenlist.json';
import { getJsonRpcProvider } from '../connectors/MyJsonRpcProvider'; 

export async function getPairDataWithSymbol(chainId: ChainId, tokenA: Token, tokenB: Token) : Promise<[Pair, string]> {
    try {
        const thePair = await Fetcher.fetchPairData(tokenA, tokenB, getJsonRpcProvider(chainId));
        const [s1, s2] = getPairSymbolByAddress(chainId, thePair.token0.address, thePair.token1.address);
        return [thePair, `${s1}/${s2}`];
    } catch (err) {
        throw new Error(`\`${tokenA.symbol}/${tokenB.symbol}\`查询PariData失败`);
    }
}

export async function getTokenDataWithPairSymbol(chainId: ChainId, tokenA: string, tokenB: string) : Promise<[Token, Token, string]> {
    const _tokenA = await getTokenDataByAddr(chainId, tokenA);
    const _tokenB = await getTokenDataByAddr(chainId, tokenB);
    const [s1, s2] = getPairSymbolByAddress(chainId, _tokenA.address, _tokenB.address);
    return [_tokenA, _tokenB, `${s1}/${s2}`];
}

export async function getTokenDataByAddress(chainId: ChainId, tokenA: string, tokenB: string) : Promise<[Token, Token]> {
    const _tokenA = await getTokenDataByAddr(chainId, tokenA);
    const _tokenB = await getTokenDataByAddr(chainId, tokenB);
    return [_tokenA, _tokenB];
}

export async function getTokenDataByAddr(chainId: ChainId, tokenAddress: string) : Promise<Token> {
    return await Fetcher.fetchTokenData(chainId, tokenAddress, getJsonRpcProvider(chainId));
}

export function getPairSymbolByAddress(chainId: ChainId, tokenA: string, tokenB : string) : [string, string] {
    const token0: any = getTokenObjectByAddress(chainId, tokenA); 
    const token1: any = getTokenObjectByAddress(chainId, tokenB);
    const token0Symbol: string = token0 ? token0.symbol : tokenA;
    const token1Symbol: string = token1 ? token1.symbol : tokenB;
    return [ token0Symbol, token1Symbol ];
}

export function getTokenObjectByAddress(chainId: ChainId, tokenAddress: string) : any | undefined {
    const _token: any = DefaultTokenList.find(t => t.chainId == chainId && (tokenAddress.toLowerCase() === t.address.toLowerCase()));
    if (!_token) {
        // 查链
    }
    return _token;
}

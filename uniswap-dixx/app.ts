import express from 'express';
import * as http from 'http';
import * as bodyparser from 'body-parser';
import cors from 'cors'
import debug from 'debug';
import logger from 'morgan';

import { CommonRoutesConfig } from './common/common.routes.config';
import { UsersRoutes } from './routers/users/users.routes.config';
import { UniV2sRoutes } from './routers/univ2/univ2.routes.config';
import { UniV2SimplesRoutes } from './routers/univ2/univ2-simple.routes.config';

import { ErrorHandler } from './common/error';

const app: express.Application = express();
const server: http.Server = http.createServer(app);
const port: number = 3000;
const routes: Array<CommonRoutesConfig> = [];
const loggerInfo: debug.IDebugger = debug('app');
const loggerError: debug.IDebugger = debug('app-error');

app.use(logger('combined'));
app.use(bodyparser.json());
app.use(cors());

routes.push(new UsersRoutes(app));
routes.push(new UniV2sRoutes(app));
routes.push(new UniV2SimplesRoutes(app));

app.get('/', (req: express.Request, res: express.Response) => {
    res.status(200).send(`Server running at http://localhost:${port}`)
});

app.use((err: Error, req: express.Request, res: express.Response, next: express.NextFunction) => {
    ErrorHandler.handle(500, err, res);
});

process.on('unhandledRejection', (err: Error) => {
    loggerError(`unhandledRejection: errorName=${err.name}, errorMessage=${err.message}`);
});

server.listen(port, () => {
    loggerInfo(`Server running at http://localhost:${port}`);
    routes.forEach((route: CommonRoutesConfig) => {
        loggerInfo(`Routes configured for ${route.getName()}`);
    });
});
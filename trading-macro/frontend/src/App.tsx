import {
  Admin,
  Resource,
  List,
  Datagrid,
  TextField,
  DateField,
  TextInput,
  Create,
  Edit,
  SimpleForm,
  SelectInput,
  NumberInput,
  DateTimeInput,
} from 'react-admin';
import { authProvider } from './authProvider';
import { dataProvider } from './dataProvider';
import { theme } from './theme';
import './app.css';

const StrategyList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
      <TextField source="assetClass" />
      <TextField source="timeframe" />
      <TextField source="riskLevel" />
      <TextField source="status" />
      <DateField source="createdAt" />
    </Datagrid>
  </List>
);

const StrategyCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="name" fullWidth />
      <TextInput source="description" fullWidth multiline />
      <TextInput source="assetClass" />
      <TextInput source="timeframe" />
      <TextInput source="riskLevel" />
      <SelectInput
        source="status"
        choices={[
          { id: 'DRAFT', name: 'DRAFT' },
          { id: 'LIVE', name: 'LIVE' },
          { id: 'PAUSED', name: 'PAUSED' },
          { id: 'ARCHIVED', name: 'ARCHIVED' },
        ]}
        defaultValue="DRAFT"
      />
    </SimpleForm>
  </Create>
);

const StrategyEdit = () => (
  <Edit>
    <SimpleForm>
      <TextInput source="name" fullWidth />
      <TextInput source="description" fullWidth multiline />
      <TextInput source="assetClass" />
      <TextInput source="timeframe" />
      <TextInput source="riskLevel" />
      <SelectInput
        source="status"
        choices={[
          { id: 'DRAFT', name: 'DRAFT' },
          { id: 'LIVE', name: 'LIVE' },
          { id: 'PAUSED', name: 'PAUSED' },
          { id: 'ARCHIVED', name: 'ARCHIVED' },
        ]}
      />
    </SimpleForm>
  </Edit>
);

const MacroList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
      <TextField source="schedule" />
      <TextField source="status" />
      <DateField source="lastRun" />
      <DateField source="nextRun" />
    </Datagrid>
  </List>
);

const MacroCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="name" fullWidth />
      <TextInput source="schedule" fullWidth />
      <SelectInput
        source="status"
        choices={[
          { id: 'ACTIVE', name: 'ACTIVE' },
          { id: 'PAUSED', name: 'PAUSED' },
          { id: 'FAILED', name: 'FAILED' },
        ]}
        defaultValue="ACTIVE"
      />
      <DateTimeInput source="lastRun" />
      <DateTimeInput source="nextRun" />
      <NumberInput source="strategyId" />
    </SimpleForm>
  </Create>
);

const MacroEdit = () => (
  <Edit>
    <SimpleForm>
      <TextInput source="name" fullWidth />
      <TextInput source="schedule" fullWidth />
      <SelectInput
        source="status"
        choices={[
          { id: 'ACTIVE', name: 'ACTIVE' },
          { id: 'PAUSED', name: 'PAUSED' },
          { id: 'FAILED', name: 'FAILED' },
        ]}
      />
      <DateTimeInput source="lastRun" />
      <DateTimeInput source="nextRun" />
      <NumberInput source="strategyId" />
    </SimpleForm>
  </Edit>
);

const TradeList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="symbol" />
      <TextField source="side" />
      <TextField source="quantity" />
      <TextField source="price" />
      <DateField source="executedAt" />
    </Datagrid>
  </List>
);

const TradeCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="symbol" />
      <SelectInput
        source="side"
        choices={[
          { id: 'BUY', name: 'BUY' },
          { id: 'SELL', name: 'SELL' },
        ]}
        defaultValue="BUY"
      />
      <NumberInput source="quantity" />
      <NumberInput source="price" />
      <DateTimeInput source="executedAt" />
      <NumberInput source="strategyId" />
      <NumberInput source="portfolioId" />
    </SimpleForm>
  </Create>
);

const TradeEdit = () => (
  <Edit>
    <SimpleForm>
      <TextInput source="symbol" />
      <SelectInput
        source="side"
        choices={[
          { id: 'BUY', name: 'BUY' },
          { id: 'SELL', name: 'SELL' },
        ]}
      />
      <NumberInput source="quantity" />
      <NumberInput source="price" />
      <DateTimeInput source="executedAt" />
      <NumberInput source="strategyId" />
      <NumberInput source="portfolioId" />
    </SimpleForm>
  </Edit>
);

const PortfolioList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
      <TextField source="baseCurrency" />
      <TextField source="totalValue" />
      <DateField source="updatedAt" />
    </Datagrid>
  </List>
);

const PortfolioCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="name" />
      <TextInput source="baseCurrency" defaultValue="KRW" />
      <NumberInput source="totalValue" />
    </SimpleForm>
  </Create>
);

const PortfolioEdit = () => (
  <Edit>
    <SimpleForm>
      <TextInput source="name" />
      <TextInput source="baseCurrency" />
      <NumberInput source="totalValue" />
      <DateTimeInput source="updatedAt" />
    </SimpleForm>
  </Edit>
);

const RiskPolicyList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
      <TextField source="status" />
      <TextField source="maxDailyLoss" />
      <TextField source="maxPositionSize" />
      <TextField source="maxLeverage" />
      <TextField source="allowedAssetClasses" />
      <DateField source="updatedAt" />
    </Datagrid>
  </List>
);

const RiskPolicyCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="name" fullWidth />
      <NumberInput source="maxDailyLoss" />
      <NumberInput source="maxPositionSize" />
      <NumberInput source="maxLeverage" />
      <TextInput source="allowedAssetClasses" fullWidth />
      <SelectInput
        source="status"
        choices={[
          { id: 'ACTIVE', name: 'ACTIVE' },
          { id: 'REVIEW', name: 'REVIEW' },
          { id: 'DISABLED', name: 'DISABLED' },
        ]}
        defaultValue="ACTIVE"
      />
      <NumberInput source="portfolioId" />
      <DateTimeInput source="updatedAt" />
    </SimpleForm>
  </Create>
);

const RiskPolicyEdit = () => (
  <Edit>
    <SimpleForm>
      <TextInput source="name" fullWidth />
      <NumberInput source="maxDailyLoss" />
      <NumberInput source="maxPositionSize" />
      <NumberInput source="maxLeverage" />
      <TextInput source="allowedAssetClasses" fullWidth />
      <SelectInput
        source="status"
        choices={[
          { id: 'ACTIVE', name: 'ACTIVE' },
          { id: 'REVIEW', name: 'REVIEW' },
          { id: 'DISABLED', name: 'DISABLED' },
        ]}
      />
      <NumberInput source="portfolioId" />
      <DateTimeInput source="updatedAt" />
    </SimpleForm>
  </Edit>
);

const PerformanceList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="snapshotDate" />
      <TextField source="pnl" />
      <TextField source="returnPct" />
      <TextField source="drawdownPct" />
      <TextField source="sharpeRatio" />
      <TextField source="volatilityPct" />
    </Datagrid>
  </List>
);

const PerformanceCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="snapshotDate" />
      <NumberInput source="pnl" />
      <NumberInput source="returnPct" />
      <NumberInput source="drawdownPct" />
      <NumberInput source="sharpeRatio" />
      <NumberInput source="volatilityPct" />
      <NumberInput source="portfolioId" />
    </SimpleForm>
  </Create>
);

const PerformanceEdit = () => (
  <Edit>
    <SimpleForm>
      <TextInput source="snapshotDate" />
      <NumberInput source="pnl" />
      <NumberInput source="returnPct" />
      <NumberInput source="drawdownPct" />
      <NumberInput source="sharpeRatio" />
      <NumberInput source="volatilityPct" />
      <NumberInput source="portfolioId" />
    </SimpleForm>
  </Edit>
);

const PerformanceSummaryList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="period" />
      <TextField source="periodStart" />
      <TextField source="periodEnd" />
      <TextField source="returnPct" />
      <TextField source="benchmarkReturnPct" />
      <TextField source="excessReturnPct" />
      <TextField source="maxDrawdownPct" />
      <TextField source="winRatePct" />
      <TextField source="profitFactor" />
      <TextField source="benchmarkName" />
    </Datagrid>
  </List>
);

const PerformanceSummaryCreate = () => (
  <Create>
    <SimpleForm>
      <SelectInput
        source="period"
        choices={[
          { id: 'DAILY', name: 'DAILY' },
          { id: 'WEEKLY', name: 'WEEKLY' },
          { id: 'MONTHLY', name: 'MONTHLY' },
          { id: 'QUARTERLY', name: 'QUARTERLY' },
          { id: 'YEARLY', name: 'YEARLY' },
        ]}
        defaultValue="MONTHLY"
      />
      <TextInput source="periodStart" />
      <TextInput source="periodEnd" />
      <NumberInput source="returnPct" />
      <NumberInput source="benchmarkReturnPct" />
      <NumberInput source="excessReturnPct" />
      <NumberInput source="maxDrawdownPct" />
      <NumberInput source="winRatePct" />
      <NumberInput source="profitFactor" />
      <TextInput source="benchmarkName" />
      <NumberInput source="portfolioId" />
      <NumberInput source="strategyId" />
    </SimpleForm>
  </Create>
);

const PerformanceSummaryEdit = () => (
  <Edit>
    <SimpleForm>
      <SelectInput
        source="period"
        choices={[
          { id: 'DAILY', name: 'DAILY' },
          { id: 'WEEKLY', name: 'WEEKLY' },
          { id: 'MONTHLY', name: 'MONTHLY' },
          { id: 'QUARTERLY', name: 'QUARTERLY' },
          { id: 'YEARLY', name: 'YEARLY' },
        ]}
      />
      <TextInput source="periodStart" />
      <TextInput source="periodEnd" />
      <NumberInput source="returnPct" />
      <NumberInput source="benchmarkReturnPct" />
      <NumberInput source="excessReturnPct" />
      <NumberInput source="maxDrawdownPct" />
      <NumberInput source="winRatePct" />
      <NumberInput source="profitFactor" />
      <TextInput source="benchmarkName" />
      <NumberInput source="portfolioId" />
      <NumberInput source="strategyId" />
    </SimpleForm>
  </Edit>
);

const TeamList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
    </Datagrid>
  </List>
);

const TeamCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="name" />
    </SimpleForm>
  </Create>
);

const TeamEdit = () => (
  <Edit>
    <SimpleForm>
      <TextInput source="name" />
    </SimpleForm>
  </Edit>
);

const DeskList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
    </Datagrid>
  </List>
);

const DeskCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="name" />
      <NumberInput source="teamId" />
    </SimpleForm>
  </Create>
);

const DeskEdit = () => (
  <Edit>
    <SimpleForm>
      <TextInput source="name" />
      <NumberInput source="teamId" />
    </SimpleForm>
  </Edit>
);

const BookList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
    </Datagrid>
  </List>
);

const BookCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="name" />
      <NumberInput source="deskId" />
    </SimpleForm>
  </Create>
);

const BookEdit = () => (
  <Edit>
    <SimpleForm>
      <TextInput source="name" />
      <NumberInput source="deskId" />
    </SimpleForm>
  </Edit>
);

const MenuList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="code" />
      <TextField source="name" />
      <TextField source="path" />
      <TextField source="parentId" />
      <TextField source="sortOrder" />
      <TextField source="status" />
    </Datagrid>
  </List>
);

const MenuCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="code" />
      <TextInput source="name" />
      <TextInput source="path" />
      <NumberInput source="parentId" />
      <NumberInput source="sortOrder" />
      <SelectInput
        source="status"
        choices={[
          { id: 'ACTIVE', name: 'ACTIVE' },
          { id: 'HIDDEN', name: 'HIDDEN' },
        ]}
        defaultValue="ACTIVE"
      />
    </SimpleForm>
  </Create>
);

const MenuEdit = () => (
  <Edit>
    <SimpleForm>
      <TextInput source="code" />
      <TextInput source="name" />
      <TextInput source="path" />
      <NumberInput source="parentId" />
      <NumberInput source="sortOrder" />
      <SelectInput
        source="status"
        choices={[
          { id: 'ACTIVE', name: 'ACTIVE' },
          { id: 'HIDDEN', name: 'HIDDEN' },
        ]}
      />
    </SimpleForm>
  </Edit>
);

const MenuPermissionList = () => (
  <List>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="role" />
      <TextField source="menu.id" />
      <TextField source="menu.name" />
      <TextField source="canView" />
      <TextField source="canEdit" />
    </Datagrid>
  </List>
);

const MenuPermissionCreate = () => (
  <Create>
    <SimpleForm>
      <SelectInput
        source="role"
        choices={[
          { id: 'ADMIN', name: 'ADMIN' },
          { id: 'TRADER', name: 'TRADER' },
        ]}
      />
      <NumberInput source="menuId" />
      <SelectInput
        source="canView"
        choices={[
          { id: true, name: 'true' },
          { id: false, name: 'false' },
        ]}
        defaultValue={true}
      />
      <SelectInput
        source="canEdit"
        choices={[
          { id: true, name: 'true' },
          { id: false, name: 'false' },
        ]}
        defaultValue={false}
      />
    </SimpleForm>
  </Create>
);

const MenuPermissionEdit = () => (
  <Edit>
    <SimpleForm>
      <SelectInput
        source="role"
        choices={[
          { id: 'ADMIN', name: 'ADMIN' },
          { id: 'TRADER', name: 'TRADER' },
        ]}
      />
      <NumberInput source="menuId" />
      <SelectInput
        source="canView"
        choices={[
          { id: true, name: 'true' },
          { id: false, name: 'false' },
        ]}
      />
      <SelectInput
        source="canEdit"
        choices={[
          { id: true, name: 'true' },
          { id: false, name: 'false' },
        ]}
      />
    </SimpleForm>
  </Edit>
);

const App = () => (
  <Admin dataProvider={dataProvider} authProvider={authProvider} theme={theme}>
    <Resource name="strategies" list={StrategyList} create={StrategyCreate} edit={StrategyEdit} />
    <Resource name="macros" list={MacroList} create={MacroCreate} edit={MacroEdit} />
    <Resource name="trades" list={TradeList} create={TradeCreate} edit={TradeEdit} />
    <Resource name="portfolios" list={PortfolioList} create={PortfolioCreate} edit={PortfolioEdit} />
    <Resource name="risk-policies" list={RiskPolicyList} create={RiskPolicyCreate} edit={RiskPolicyEdit} />
    <Resource name="performance" list={PerformanceList} create={PerformanceCreate} edit={PerformanceEdit} />
    <Resource name="performance-summaries" list={PerformanceSummaryList} create={PerformanceSummaryCreate} edit={PerformanceSummaryEdit} />
    <Resource name="teams" list={TeamList} create={TeamCreate} edit={TeamEdit} />
    <Resource name="desks" list={DeskList} create={DeskCreate} edit={DeskEdit} />
    <Resource name="books" list={BookList} create={BookCreate} edit={BookEdit} />
    <Resource name="menus" list={MenuList} create={MenuCreate} edit={MenuEdit} />
    <Resource name="menu-permissions" list={MenuPermissionList} create={MenuPermissionCreate} edit={MenuPermissionEdit} />
  </Admin>
);

export default App;

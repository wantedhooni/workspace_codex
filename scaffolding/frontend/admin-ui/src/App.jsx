import React from 'react'
import { Admin, Resource, ListGuesser, EditGuesser } from 'react-admin'

import { authProvider } from './authProvider'
import { dataProvider } from './dataProvider'

const App = () => {
  return (
    <Admin dataProvider={dataProvider} authProvider={authProvider} requireAuth>
      <Resource name="admins" list={ListGuesser} edit={EditGuesser} />
    </Admin>
  )
}

export default App

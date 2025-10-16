import {createRoot} from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import log from 'loglevel'

log.setLevel('debug')
log.getLogger('render').setLevel('debug')

/*
createRoot(document.getElementById('root')!).render(
<StrictMode>
  <App/>
</StrictMode>,
)*/
createRoot(document.getElementById('root')!).render(
    <App/>
)

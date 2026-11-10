import { AuthProvider } from "./context/AuthContext.jsx";
import { PrefsProvider } from "./context/PrefsContext.jsx";
import { NotifProvider } from "./context/NotifContext.jsx";
import { TutorProvider } from "./context/TutorContext.jsx";
import { ModoLecturaProvider } from "./context/ModoLecturaContext.jsx";
import { Narrador } from "./components/layout/Narrador.jsx";
import AvisoSesion from "./components/layout/AvisoSesion.jsx";
import AppRouter from "./routes/AppRouter.jsx";

export default function App() {
  return (
    <AuthProvider>
      <PrefsProvider>
        <ModoLecturaProvider>
        <NotifProvider>
          <TutorProvider>
            <Narrador />
            <AvisoSesion />
            <AppRouter />
          </TutorProvider>
        </NotifProvider>
        </ModoLecturaProvider>
      </PrefsProvider>
    </AuthProvider>
  );
}

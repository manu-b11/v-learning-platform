import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "../pages/Home";
import Login from "../pages/Login";
import Register from "../pages/Register";
import Dashboard from "../pages/Dashboard";
import Courses from "../pages/Courses";
import CourseDetail from "../pages/CourseDetail";
import ModuleContent from "../pages/ModuleContent";
import Evaluations from "../pages/Evaluations";
import TakeEvaluation from "../pages/TakeEvaluation";
import EvaluationResult from "../pages/EvaluationResult";
import Profile from "../pages/Profile";
import Settings from "../pages/Settings";

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/courses" element={<Courses />} />
        <Route path="/courses/:id" element={<CourseDetail />} />
        <Route
          path="/courses/:id/modules/:moduleId"
          element={<ModuleContent />}
        />
        <Route path="/evaluations" element={<Evaluations />} />
        <Route path="/evaluations/:id" element={<TakeEvaluation />} />
        <Route path="/evaluations/:id/result" element={<EvaluationResult />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/settings" element={<Settings />} />
      </Routes>
    </BrowserRouter>
  );
}

export default AppRouter;

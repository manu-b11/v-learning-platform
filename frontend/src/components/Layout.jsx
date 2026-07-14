import Navbar from "./Navbar";
import Sidebar from "./Sidebar";

function Layout({ children }) {
  return (
    <div className="flex">
      <Sidebar />

      <div className="ml-64 flex-1 bg-background">
        <Navbar />

        <main className="pt-3 pb-6 px-6">{children}</main>
      </div>
    </div>
  );
}

export default Layout;

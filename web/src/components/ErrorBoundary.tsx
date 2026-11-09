import { Component, ErrorInfo, ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Unhandled application error:', error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center p-6 bg-paper-light dark:bg-paper-dark text-chinese-ink dark:text-gray-100">
          <div className="max-w-md w-full bg-paper-card dark:bg-paper-cardDark border border-paper-border dark:border-paper-borderDark rounded-2xl p-6 shadow-soft text-center space-y-4">
            <div className="text-4xl">🍵</div>
            <h2 className="text-lg font-bold font-display">
              Something went sideways
            </h2>
            <p className="text-xs text-gray-600 dark:text-gray-400 font-medium">
              An unexpected error occurred while processing flashcard data.
            </p>
            {this.state.error && (
              <pre className="p-3 rounded-lg bg-gray-100 dark:bg-gray-800 text-[11px] text-left overflow-x-auto text-chinese-red font-mono">
                {this.state.error.message}
              </pre>
            )}
            <button
              onClick={() => window.location.reload()}
              className="px-4 py-2 rounded-xl text-xs font-bold bg-chinese-red hover:bg-chinese-redHover text-white transition shadow-sm"
            >
              Reload Application
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

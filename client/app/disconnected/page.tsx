import React from "react";

const DisconnectedPage: React.FC = () => {
  return (
    <div className='flex items-center justify-center h-screen bg-gray-100'>
      <div className='bg-white p-8 rounded-lg shadow-md text-center'>
        <h2 className='text-2xl font-bold mb-4'>You have been disconnected</h2>
        <p>
          Your session data was cleared. You can return to the main screen to
          start a new game.
        </p>
      </div>
    </div>
  );
};

export default DisconnectedPage;
